# -*- coding: utf-8 -*-

import time
import socket
import threading
import random
import math
import os

#############################" VARIABLES ##############################"
# "LAPTOP-IT1VP3Q2"


DATA = 1024

hauteur = 250.0
largeur = 250.0


mutexVehicules = threading.Lock()
vehicules = dict()

mutexNewCom = threading.Lock()
newCommandes = dict()
acceptingNewCommands = True

mutexObjectif = threading.Lock()
newObjectifEvent = threading.Event()

finTimer = threading.Event()

server_refresh_tickrate = threading.Event()

MAX_VITESSE = 3.0
MAX_DECALAGE = 2*MAX_VITESSE

##########################################################################
def distance(x1,y1,x2,y2,rad):
    if (x2 >= x1 - rad ) and (x2 <= x1 + rad ):
        if (y2 >= y1 - rad ) and (y2 <= y1 + rad ):
            return True
    return False

class Ship():
    def __init__(self, n, socjet):
        self.name = n
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        self.direction = -float(math.pi/2)
        self.vX = 0.0
        self.vY = 0.0
        self.score = 0
        self.clientSock = socjet
        
    def __repr__(self):
        s = self.name + ":X" +str(self.posX)+":Y"+str(self.posY)
        return s
    
    def toString(self):
        s = self.name + ":X" +str(self.posX)+":Y"+str(self.posY)
        return s

    def resetPos(self):
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)

class Objectif():
    def __init__(self):
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        self.valeur = 1 # Pour différencier si jamais on met plusieurs types d'objets récupérables
        self.rayon = 20.0
        
    def contact(self, vehicule):
        if((self.posX+self.rayon) >= vehicule.posX):
            if((self.posX-self.rayon) <= vehicule.posX):
                if((self.posY+self.rayon) >= vehicule.posY):
                    if((self.posY-self.rayon) <= vehicule.posY):
                        return True
        return False
        
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
        self.pasTimer = math.ceil(temps/10.0)
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
            server_refresh_tickrate.clear()
            time.sleep(self.temps)
            server_refresh_tickrate.set()

timer = Timer(3.0)
#############################################################################

class Connexion(threading.Thread):
    def __init__(self,sock,add):
        threading.Thread.__init__(self)
        self.clientSock = sock
        self.addr = add
        self.name = ""
        
    def run(self):
        global timer
        print ("Handling")
        
        while True:
            request = self.clientSock.recv(DATA)
            reply = (request.decode()).split("/")
            if (reply[0] == "CONNECT"):
                try:
                    mutexVehicules.acquire()
                    
                    if(reply[1] in vehicules):
                        m = "DENIED\n"
                        self.clientSock.send(m.encode())
                        del m
                        continue
                    else: 
                        sessionEnCours = False

                        if(len(vehicules)==0):
                            # Lance le timer si c'est le premier joueur connecte
                            timer.start()
                        else:
                            # Previens les autres joueurs de sa connexion sinon
                            m = "NEWPLAYER/" + str(reply[1]) + "\n"
                            for (pseudo, vaisseau) in vehicules.items():
                                vaisseau.clientSock.send(m.encode())
                        
                        vehicules[reply[1]] = Ship(reply[1], self.clientSock)
                        self.name = reply[1]  
                        
                        m = "WELCOME/" 
                        if(timer.decompte < timer.temps):
                            m += "ATTENTE/"
                        else:
                            m += "JEU/"
                            sessionEnCours = True

                        for (nom, vehicule) in vehicules.items():
                            m+= nom + ":" + str(vehicule.score) +"|"
                        m = m[:-1]
                        m += "/"


                        try:
                            mutexObjectif.acquire()
                            m += "X" + str(objectif.posX)+"Y"+str(objectif.posY)
                            m += "\n"

                            self.clientSock.send(m.encode())
                            
                            if(sessionEnCours):
                                m = "SESSION/"


                                for (joueur, ship) in vehicules.items():
                                    m += joueur + ":X" + str(ship.posX) + ":Y" + str(ship.posY) + "|"

                                m = m[:-1]
                                m +="/"
                                m += "X" + str(objectif.posX)+"Y"+str(objectif.posY)
                                m += "\n"
                                self.clientSock.send(m.encode())
                        finally:
                            mutexObjectif.release()
                finally:
                    mutexVehicules.release()
                
                del m  
                del sessionEnCours
                
            if (reply[0] == "EXIT"):
                m = "PLAYERLEFT/"+ str(reply[1])+"\n"
                try:
                    mutexVehicules.acquire()
                    if(len(vehicules)==1):
                            timer.zeroJoueur.set()
                            timer = Timer(3)
                    
                    for (pseudo, vaisseau) in vehicules.items():
                        vaisseau.clientSock.send(m.encode())
                    try:
                        vehicules.pop(reply[1])
                        mutexNewCom.acquire()
                        newCommandes.pop(reply[1])
                        self.name = None
                    except KeyError:
                        print("this player is not in here")
                finally:
                    mutexNewCom.release()
                    mutexVehicules.release()
                    
                
            if(reply[0] == "NEWPOS"):
                c =reply[1][1:]
                rc = c.split('Y')
                del c
                posX = float(rc[0])
                posY = float(rc[1])
                m = ""
                try:
                    mutexVehicules.acquire()
                    if(self.name in vehicules.keys()):
                        v = vehicules[self.name]
                        if(((posX + largeur - v.posX + largeur ) < (MAX_DECALAGE + 2*largeur)) and ((posY + hauteur - v.posY + hauteur ) < (MAX_DECALAGE + 2*hauteur))):
                            v.posX = posX
                            v.posY = posY

                        else:
                            m += "NEWPOS/" + self.name +"/X" + str(v.posX) + "Y" + str(v.posY) +"\n"
                            self.clientSock.send(m.encode()) 
                finally:
                    mutexVehicules.release()
                del rc 
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
                        if(self.name in vehicules):
                            newCommandes[self.name]=com
                    finally:
                        mutexNewCom.release()
                        del a
                        del t
                        del com

            if(reply[0] == "ENVOI"):
                m = "RECEPTION/>>>"+self.name+" : "+str(reply[1])+"\n"
                try:
                    mutexVehicules.acquire()                    
                    for (pseudo, vaisseau) in vehicules.items():
                        vaisseau.clientSock.send(m.encode())
                finally:
                    mutexVehicules.release()
                del m
            if(reply[0] == "PENVOI"):
                
                m = "PRECEPTION/"+str(reply[2])+"/"+self.name+"\n"
                try:
                    mutexVehicules.acquire() 
                    if(str(reply[1]) in vehicules):
                        for (pseudo, vaisseau) in vehicules.items():
                            if pseudo == str(reply[1]):
                                vaisseau.clientSock.send(m.encode())
                    else:
                        m = "DENIED\n"
                        self.clientSock.send(m.encode())
                finally:
                    mutexVehicules.release()
                    del m
            
            del request
            del reply
            
        print("SORTIE DE LA BOUCLE")
        self.clientSock.close() 
      
###############################################################################
      
class Arena(threading.Thread):
    def __init__(self, vejhicles):
        threading.Thread.__init__(self)
        self.vehicules = vejhicles
        self.maxScore = 5
        self.winner = False

    def run(self):
        while(True):
            print("ATTENTE")
            finTimer.wait()
            print("DEBUT DE SESSION")
            m = "SESSION/"
            try:
                mutexVehicules.acquire()
                for (joueur, ship) in self.vehicules.items():
                    m += joueur + ":X" + str(ship.posX) + ":Y" + str(ship.posY) + "|"

                m = m[:-1]
                m +="/"
                try:
                    mutexObjectif.acquire()
                    m += "X" + str(objectif.posX)+"Y"+str(objectif.posY)
                    m += "\n"
                finally:
                    mutexObjectif.release()
                m += "\n"
                for (joueur, vaisseau) in vehicules.items():
                    vaisseau.clientSock.send(m.encode())

            finally:
                mutexVehicules.release()
                del m
            tick = Tickrate(0.005)
            tick.start()
            i = 0
            while not self.winner:
                server_refresh_tickrate.wait()
                if(len(vehicules)== 0):
                    break
                self.computeCommands()
                i += 1
            if(self.winner):
                try:
                    mutexVehicules.acquire()
                    w = "WINNER/"
                    for (joueur, ship) in vehicules.items():
                        w += joueur + ":" + str(ship.score)+"|"
                    w = w[:-1]
                    w += "\n"
                    for (joueur, s) in self.vehicules.items():
                            s.clientSock.send(w.encode())
                finally:
                    mutexVehicules.release()

            tick.finDeSession.set()
            finTimer.clear() 

    def computeCommands(self):
        global newCommandes
        global acceptingNewCommands
        global MAX_VITESSE
        global hauteur
        global largeur
        reponse = "TICK/"
        newObj = "NEWOBJ/"
        try:
            mutexVehicules.acquire()
            mutexNewCom.acquire()
            if(len(newCommandes) > 0):
                acceptingNewCommands = False                
                for (k, v) in newCommandes.items():
                    try:
                        vehicule = self.vehicules[k]
                    except Exception:
                        print(k," left")
                        continue
                    a,t = v.split(":")
                    vehicule.direction += float(a)
                    vehicule.posX += largeur
                    vehicule.posY += hauteur
                    
                    nouvelleVitesseX = vehicule.vX + float(t)*math.cos(vehicule.direction)
                    nouvelleVitesseY = vehicule.vY + float(t)*math.sin(vehicule.direction)

                    if(nouvelleVitesseX > 0):
                        if(nouvelleVitesseY > 0):
                            vehicule.vX = min(nouvelleVitesseX, MAX_VITESSE)
                            vehicule.vY = min(nouvelleVitesseY, MAX_VITESSE)
                        else:
                            vehicule.vX = min(nouvelleVitesseX, MAX_VITESSE)
                            vehicule.vY = max(nouvelleVitesseY, -MAX_VITESSE)
                    else:
                        if(nouvelleVitesseY > 0):
                            vehicule.vX = max(nouvelleVitesseX, -MAX_VITESSE)
                            vehicule.vY = min(nouvelleVitesseY, MAX_VITESSE)
                        else:
                            vehicule.vX = max(nouvelleVitesseX, -MAX_VITESSE)
                            vehicule.vY = max(nouvelleVitesseY, -MAX_VITESSE)

                    vehicule.posX = (vehicule.vX + vehicule.posX + largeur*2)%(2*largeur)
                    vehicule.posY = (vehicule.vY + vehicule.posY + hauteur*2)%(2*hauteur)
                
                    vehicule.posX -= largeur
                    vehicule.posY -= hauteur
                    try:
                        mutexObjectif.acquire()
                        if(objectif.contact(vehicule)):
                            vehicule.score +=objectif.valeur;
                            if(vehicule.score >=self.maxScore):
                                self.winner = True
                            else:
                                print("reset pos")
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
                for (joueur, s) in self.vehicules.items():
                    s.clientSock.send(reponse.encode())
                if(len(newObj)>8):
                    print(newObj)
                    for (joueur, s) in self.vehicules.items():
                        s.clientSock.send(newObj.encode())
        finally:
            acceptingNewCommands = True
            mutexNewCom.release()
            mutexVehicules.release()
            
###############################################################################
class Server:
     def __init__(self,host,port):
        self.sockServ = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        try:
            self.sockServ.bind((host,port))
        except socket.error:
            print("Binding to ",host," at port : ",port, "failed")
            
        self.sockServ.listen(10)
        
        print("------- Server ready --------\n")
        print("------- Host : "+host+" -----\n")
        print("------- Port : "+str(port)+" -----\n")
        Arena(vehicules).start() 
        while True:
            clientSock, addr = self.sockServ.accept()
            print("------- Client connected ------\n")
            t = Connexion(clientSock,addr);
            t.start()         


while True:
    """print ("Veuillez entre le hostname : ")
    h = input()"""
    print ("Veuillez entre le port : ")
    p = input()
    h = os.uname()[1]
    server = Server(h,int(p))