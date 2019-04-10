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

hauteur = 400.0
largeur = 400.0

mutexJoueurs = threading.Lock()
joueurs = dict()

mutexVehicules = threading.Lock()
vehicules = dict()

mutexNewCom = threading.Lock()
newCommandes = dict()
acceptingNewCommands = True

mutexObjectif = threading.Lock()
newObjectifEvent = threading.Event()

finTimer = threading.Event()

server_refresh_tickrate = threading.Event()

MAX_VITESSE = 6.0

##########################################################################
def distance(x1,y1,x2,y2):
    return math.sqrt( (x2 - x1)**2 + (y2 - y1)**2 )
    

class Ship():
    def __init__(self, n):
        self.name = n
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        self.direction = float(math.pi/2)
        self.vX = 0.0
        self.vY = 0.0
        self.score = 0
        
    def __repr__(self):
        s = self.name + ":X" +str(self.posX)+":Y"+str(self.posY)
        return s
    
    def toString(self):
        s = self.name + ":X" +str(self.posX)+":Y"+str(self.posY)
        return s

class Objectif():
    def __init__(self):
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        self.valeur = 1 # Pour différencier si jamais on met plusieurs types d'objets récupérables

    def resetPos(self):
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        
objectif = Objectif()
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

class Tickrate(threading.Thread):
    def __init__(self, temps):
        threading.Thread.__init__(self)
        self.temps = temps
        self.finDeSession = threading.Event()
        
    def run(self):
        while not self.finDeSession.is_set():
            time.sleep(self.temps)
            server_refresh_tickrate.set()

timer = Timer(3)
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
                print("LA CONNEXION")
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
                    finally:
                        mutexJoueurs.release()
                        self.name = reply[1]
                        v = Ship(reply[1])
                        self.ship = v
                    try:
                        mutexVehicules.acquire()
                        vehicules[reply[1]] = v
                        for (nom, vehicule) in vehicules.items():
                            m+= nom + ":" + str(vehicule.score) +"|"
                        m = m[:-1]
                        m += "/"
                    finally:
                        mutexVehicules.release()
                    try:
                        mutexObjectif.acquire()
                        m += "X" + str(objectif.posX)+"Y"+str(objectif.posY)
                        m += "\n"
                    finally:
                        mutexObjectif.release()
                        self.clientSock.send(m.encode())
                        del m  
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
                        timer = Timer(3)
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
                    #del m
                    #del request
                    #del reply
                    #break
                
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
        self.winner = False
        
    def run(self):
        while(True):
            print("DEBUT")
            finTimer.wait()
            print("LE VRAI DEBUT DE SESSION")
            m = "SESSION/"
            try:
                mutexVehicules.acquire()
                for (joueur, ship) in vehicules.items():
                    m += joueur + ":X" + str(ship.posX) + ":Y" + str(ship.posY) + "|"
            finally:
                mutexVehicules.release()
                m = m[:-1]
                m +="/"
                try:
                    mutexObjectif.acquire()
                    m += "X" + str(objectif.posX)+"Y"+str(objectif.posY)
                    m += "\n"
                finally:
                    mutexObjectif.release()
                m += "\n"
            try:
                mutexJoueurs.acquire()
                for (joueur, chaussette) in joueurs.items():
                    chaussette.clientSock.send(m.encode())
            finally:
                mutexJoueurs.release()
                del m
            tick = Tickrate(2)
            tick.start()
            while not self.winner:
                server_refresh_tickrate.wait()
                if(len(joueurs)== 0):
                    break
                self.computeCommands()
            if(self.winner):
                try:
                    mutexVehicules.acquire()
                    w = "WINNER/"
                    for (joueur, ship) in vehicules.items():
                        w += joueur + ":" + str(ship.score)+"|"
                    w = w[:-1]
                    w += "\n"
                    for (joueur, s) in self.joueurs.items():
                            s.clientSock.send(w.encode())
                finally:
                    mutexVehicules.release()
            tick.finDeSession.set()
            finTimer.clear() 
    
    def computeCommands(self):
        global newCommandes
        global acceptingNewCommands
        reponse = "TICK/"
        newObj = "NEWOBJ/"
        try:
            mutexNewCom.acquire()
            if(len(newCommandes) > 0):
                acceptingNewCommands = False
                for (k, v) in newCommandes.items():
                    vehicule = vehicules[k]
                    for c in v:
                        a,t = c.split(":")
                        #FAIRE MIEUX LES CLACULS CAR CA NE MARCHE POINT
                        vehicule.direction += float(a)
                        vehicule.vX = min(vehicule.vX + float(t)*math.cos(vehicule.direction),MAX_VITESSE)
                        vehicule.vY = min(vehicule.vY + float(t)*math.sin(vehicule.direction),MAX_VITESSE)

                        vehicule.posX = (vehicule.posX + vehicule.vX)
                        if(math.fabs(vehicule.posX) > largeur):
                            if(vehicule.posX > 0):
                                vehicule.posX %= largeur
                                vehicule.posX -= largeur
                            else:
                                vehicule.posX %= largeur
                                vehicule.posX = -vehicule.posX
                                vehicule.posX += largeur
                        vehicule.posY = (vehicule.posY + vehicule.vY)
                        if(math.fabs(vehicule.posY) > hauteur):
                            if(vehicule.posY > 0):
                                vehicule.posY %= hauteur
                                vehicule.posY -= hauteur
                            else:
                                vehicule.posY %= hauteur
                                vehicule.posY = -vehicule.posY
                                vehicule.posY += hauteur
                        try:
                            mutexObjectif.acquire()
                            if(distance(objectif.posX,objectif.posY,vehicule.posX,vehicule.posY)<30.0):
                                vehicule.score +=objectif.valeur;
                                if(vehicule.score >=self.maxScore):
                                    print("wiiiiiiiiiiiiiiiiiii") #winner
                                    self.winner = True
                                else:
                                    objectif.resetPos()
                                    tmpx = "X"+str(objectif.posX)
                                    tmpy = "Y"+str(objectif.posY)
                                    newObj+=tmpx+tmpy+"/"
                                    for (nom, vehicule) in vehicules.items():
                                        newObj+= nom + ":" + str(vehicule.score) +"|"
                                        newObj = newObj[:-1]
                                        newObj += "/"
                                    newObj += "\n"
                        finally:
                            mutexObjectif.release()
                    reponse += str(k)+":"+"X"+str(vehicule.posX)+"Y"+str(vehicule.posY)+"VX"+str(vehicule.vX)+"VY"+str(vehicule.vY)+"T"+str(vehicule.direction)+"|"
                newCommandes = dict()
                reponse = reponse[:-1]
                reponse += "\n"
                for (joueur, s) in self.joueurs.items():
                    print(reponse)
                    s.clientSock.send(reponse.encode())
                if(len(newObj)>8):
                    print("sending new obj")
                    for (joueur, s) in self.joueurs.items():
                        s.clientSock.send(newObj.encode())
                    
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
