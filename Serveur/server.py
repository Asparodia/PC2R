import time
import socket
import threading
import os
import random


#windows : hostname sur terminal , mine is LAPTOP-IT1VP3Q2
#############################" VARIABLES ##############################"
H = os.uname()[1]
print(H)
P = 2019
DATA = 1024

hauteur = 20.0
largeur = 20.0

mutexJoueurs = threading.Lock()
joueurs = dict()

mutexVehicules = threading.Lock()
vehicules = dict()

mutexNewCom = threading.Lock()
newCommandes = dict()
acceptingNewCommands = True

finTimer = threading.Event()

server_refresh_tickrate = threading.Event()

##########################################################################
class Ship():
    def __init__(self, n):
        self.name = n
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        self.direction = 90.0
        self.vX = 0.0
        self.vY = 0.0
        self.score = 0
        
    def __repr__(self):
        s = self.name + ":X" +str(self.posX)+":Y"+str(self.posY)
        return s
    
    def toString(self):
        s = self.name + ":X" +str(self.posX)+":Y"+str(self.posY)
        return s

##########################################################################

class Timer(threading.Thread):
    def __init__(self, temps):
        threading.Thread.__init__(self)
        self.temps = temps
        
    def run(self):
        i = 0
        while(i < self.temps):
            time.sleep(1)
            i +=1
            print(">>>>>> Il reste "+str(self.temps-i)+" secondes")
        
        global finTimer
        finTimer.set()

class Tickrate(threading.Thread):
    def __init__(self, temps):
        threading.Thread.__init__(self)
        self.temps = temps
        
    def run(self):
        i = 0
        temps_depart = self.temps
        while True:
            time.sleep(self.temps)
            server_refresh_tickrate.set()

timer = Timer(10)
tick = Tickrate(2)
#############################################################################

class Connexion(threading.Thread):
    def __init__(self,sock,add):
        threading.Thread.__init__(self)
        self.clientSock = sock
        self.addr = add
        self.name = ""
        self.ship = None
        
    def run(self):
        global timer
        print ("Handling")
        while True:
            request = self.clientSock.recv(DATA)
            reply = (request.decode()).split("/")
            
            if (reply[0] == "CONNECT"):
                if(reply[1] in joueurs):
                    m = "DENIED\n"
                    self.clientSock.send(m.encode())
                    del m
                    continue
                else:
                    m = "WELCOME/" + str(reply[1]) +"\n"
                    self.clientSock.send(m.encode())
                    del m   
                    try:
                        mutexJoueurs.acquire()
                        joueurs[reply[1]] = self
                        if(len(joueurs)==1):
                            timer.start()
                    finally:
                        mutexJoueurs.release()
                        self.name = reply[1]
                        v = Ship(reply[1])
                        self.ship = v
                    try:
                        mutexVehicules.acquire()
                        vehicules[reply[1]] = v
                    finally:
                        mutexVehicules.release()

            if (reply[0] == "EXIT"):
                m = "PLAYERLEFT/"+ str(reply[1])+"\n"
                self.clientSock.send(m.encode())
                
                try:
                    mutexJoueurs.acquire()
                    try:
                        del joueurs[reply[1]]
                    except KeyError:
                        print("this player is not in here")

                    if(len(joueurs.keys())==0):
                        timer._stop() # pb ici quand je veux partir pendant que le timer tourne
                        timer = Timer(10)
                finally:
                    mutexJoueurs.release()  
                try:
                    mutexVehicules.acquire()
                    try:
                        del vehicules[reply[1]]
                    except KeyError:
                        print("this player doesn't have a vehicle")
                    
                finally:
                    mutexVehicules.release()
                    del m
                    del request
                    del reply
                    break
                

            if(reply[0] == "NEWPOS"):
                c =reply[1][1:]
                rc = c.split('Y')
                del c
                self.ship.posX = float(rc[0])%largeur
                self.ship.posY = float(rc[1])%hauteur
                del rc
                m = "POSITION_SET\n"
                self.clientSock.send(m.encode())
                del m
                
            if(reply[0] == "NEWCOM"):
                a =reply[1][1:]
                at = a.split('T')
                a = at[0]
                t = at[1]
                com = a+":"+t
                if(acceptingNewCommands):
                    try:
                        mutexNewCom.acquire()
                        # ILFAUT FAIRE ATTENTCION ICI PARCE QUE LE BOOLEEN acceptingNewCommands
                        if(self.name not in newCommandes):
                            newCommandes[self.name] = list()
                        newCommandes[self.name].append(com)
                    finally:
                        mutexNewCom.release()
                        del a
                        del t
                        del com

            if(reply[0] == "KILL"):
                del request
                del reply
                break
            
            del request
            del reply
            
        print("SORTIE DE LA BOUCLE")
        self.clientSock.close() #mieux gerer la deco du cote client ptete quand il recoi playleft il se ferme
      
###############################################################################
      
class Arena(threading.Thread):
    def __init__(self, players, vejhicles):
        threading.Thread.__init__(self)
        self.joueurs = players
        self.vehicules = vejhicles
        self.maxScore = 5
        
    def run(self):
        finTimer.wait()
        tick.start()
        while True:
            server_refresh_tickrate.wait()
            self.computeCommands()
        tick._stop()

    def computeCommands(self):
        global newCommandes
        reponse = "TICK/"
        try:
            mutexNewCom.acquire()
            if(len(newCommandes) > 0):
                acceptingNewCommands = False
                for (k, v) in newCommandes.items():
                    vehicule = vehicules[k]
                    for c in v:
                        a,t = c.split(":")
                        #FAIRE MIEUX LES CLACULS CAR CA NE MARCHE POINT
                        vehicule.vX *= float(t)
                        vehicule.vY *= float(t)
                        vehicule.posX = (vehicule.posX+20 + vehicule.vX)%(2*largeur)
                        vehicule.posX -= largeur
                        vehicule.posY = (vehicule.posY+20 + vehicule.vY)%(2*hauteur)
                        vehicule.posY -= hauteur
                        vehicule.direction += float(a)
                    reponse += str(k)+":"+"X"+str(vehicule.posX)+"Y"+str(vehicule.posY)+"VX"+str(vehicule.vX)+"VY"+str(vehicule.vY)+"T"+str(vehicule.direction)+"|"
                newCommandes = dict()
                reponse = reponse[:-1]
                reponse += "\n"
                for (joueur, s) in self.joueurs.items():
                    print(reponse)
                    s.clientSock.send(reponse.encode())
        finally:
            acceptingNewCommands = True
            mutexNewCom.release()
        
                
###############################################################################
class Server:
     def __init__(self,host=H,port=P):
        self.sockServ = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        try:
            self.sockServ.bind((host,port))
        except socket.error:
            print("Binding to ",host," at port : ",port, "failed")
            
        self.sockServ.listen(10)
        
        print("------- Server ready --------\n")

        Arena(joueurs, vehicules).start() 
        while True:
            clientSock, addr = self.sockServ.accept()
            print("------- Client connected ------\n")
            t = Connexion(clientSock,addr);
            t.start()         
server = Server()
