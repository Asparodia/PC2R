import time
import socket
import threading
import os
import random
import math


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
        self.zeroJoueur = threading.Event()
        self.pasTimer = math.ceil(temps/10)
        self.decompte = 0
        
    def run(self):
        while(self.decompte < self.temps and not self.zeroJoueur.is_set()):
            print(">>>>>> Il reste "+str(self.temps-self.decompte)+" secondes")
            self.zeroJoueur.wait(timeout = self.pasTimer)
            self.decompte += self.pasTimer
        
        global finTimer
        finTimer.set()
        #print("FIN ITMER")

class Tickrate(threading.Thread):
    def __init__(self, temps):
        threading.Thread.__init__(self)
        self.temps = temps
        
    def run(self):
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
                # Le pseudo doit etre en lettres romaines minuscules
                else: 
                    try:
                        mutexJoueurs.acquire()
                        if(len(joueurs)==0):
                            # Lance le timer si c'est le premier joueur connecte
                            timer.start()
                        else:
                            # Previens les autres joueurs de sa connexion sinon
                            m = "NEWPLAYER/" + str(reply[1]) + "\n"
                            for (pseudo, connexion) in joueurs.items():
                                connexion.clientSock.send(m.encode())
                        joueurs[reply[1]] = self
                        m = "WELCOME/" 
                        if(timer.decompte < timer.temps):
                            m += "ATTENTE/"
                        else:
                            m += "JEU/"
                        m += "faire les scores et les joueurs ici/"
                        m += "(x,y) de l'objectif"
                        m += "\n"
                        self.clientSock.send(m.encode())
                        del m  
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
                        print(len(joueurs))
                    except KeyError:
                        print("this player is not in here")

                    if(len(joueurs.keys())==0):
                        timer.zeroJoueur.set()
                        timer = Timer(10)
                    else:
                        for (pseudo, connexion) in joueurs.items():
                            connexion.clientSock.send(m.encode())
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
        m = "SESSION/"
        try:
            mutexVehicules.acquire()
            for (joueur, ship) in vehicules.items():
                m += joueur + ":X" + str(ship.posX) + ":Y" + str(ship.posY) + "|"
        finally:
            mutexVehicules.release()
            m = m[:-1]
            m += "/mettre l'obj ici"
            m += "\n"
        try:
            mutexJoueurs.acquire()
            for (joueur, chaussette) in joueurs.items():
                chaussette.clientSock.send(m.encode())
        finally:
            mutexJoueurs.release()
            del m
        tick.start()
        while True:
            server_refresh_tickrate.wait()
            self.computeCommands()
        tick._stop()

    def computeCommands(self):
        global newCommandes
        global acceptingNewCommands
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
