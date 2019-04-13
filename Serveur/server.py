# -*- coding: utf-8 -*-

import time
import socket
import threading
import os
import random
import math

# IL FAUT MIEUX PLACER LES MUTEX

#windows : hostname sur terminal , mine is LAPTOP-IT1VP3Q2

#############################" VARIABLES ##############################"
H = os.uname()[1]
print("hostname :",H)
P = 20189
DATA = 1024

hauteur = 200.0
largeur = 200.0

# =============================================================================
# mutexJoueurs = threading.Lock()
# joueurs = dict()
# =============================================================================

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
    def __init__(self, n, socjet):
        self.name = n
        self.posX = random.uniform(-largeur, largeur)
        self.posY = random.uniform(-hauteur, hauteur)
        self.direction = -float(math.pi/2)
        self.vX = 0.0
        self.vY = 0.0
        self.score = 0
        self.clientSock = socjet
        #self.xDebutNegatif = self.posX < 0 # indique si posX est négatif à l'initialisation car pb avec les coordonnées négatives
        #self.yDebutNegatif = self.posY < 0 # indique si posY est positif à l'initialisation car pb avec les coordonnées négatives
        
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
                        
                    except KeyError:
                        print("this player is not in here")
                finally:
                    mutexNewCom.release()
                    mutexVehicules.release()
                    
                
            if(reply[0] == "NEWPOS"):
                c =reply[1][1:]
                rc = c.split('Y')
                del c
                #self.ship.posX = float(rc[0])%largeur #Bizarre ici aussi
                #self.ship.posY = float(rc[1])%hauteur # C'EST QUOI CE TRUC DE MERDE ?
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
                        # IL FAUT FAIRE ATTENTCION ICI PARCE QUE LE BOOLEEN acceptingNewCommands
                        if(self.name in vehicules):
                            newCommandes[self.name]=com
#                        if(self.name not in newCommandes):
#                            newCommandes[self.name] = "" #list et append avant
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
    def __init__(self, vejhicles):
        threading.Thread.__init__(self)
#        self.joueurs = players
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
                for (joueur, ship) in self.vehicules.items():
                    m += joueur + ":X" + str(ship.posX) + ":Y" + str(ship.posY) + "|"
#                    print(m)
#            finally:
#                mutexVehicules.release()
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
#            try:
#                mutexJoueurs.acquire()
#                for (joueur, chaussette) in joueurs.items():
#                    chaussette.clientSock.send(m.encode())
            finally:
                mutexVehicules.release()
                del m
            tick = Tickrate(0.5)
            tick.start()
            i = 0
            while not self.winner:
                server_refresh_tickrate.wait()
                if(len(vehicules)== 0):
                    break
                self.computeCommands()
                i += 1
               # print(i)
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
#                    for c in v:

                    a,t = v.split(":")
                    #FAIRE MIEUX LES CLACULS CAR CA NE MARCHE POINT
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
                    #vehicule.xDebutNegatif = False # Cette variable n'est plus nécessaire après le premier appel ?
                    #vehicule.yDebutNegatif = False # Cette variable n'est plus nécessaire après le premier appel ?
##
##                        #vitesseX = 0.0
##                        #vitesseY = 0.0
##
##                        if((vehicule.vX + float(t)) >= MAX_VITESSE):
##                            vitesseX = MAX_VITESSE
##                        elif((vehicule.vX + float(t)) <= -MAX_VITESSE):
##                            vitesseX = -MAX_VITESSE
##                        else:
##                            vitesseX = vehicule.vX + float(t)
##                        vehicule.vX = vitesseX + math.cos(vehicule.direction)
##
##                        if((vehicule.vY + float(t)) >= MAX_VITESSE):
##                            vitesseY = MAX_VITESSE
##                        elif((vehicule.vY + float(t)) <= -MAX_VITESSE):
##                            vitesseY = -MAX_VITESSE
##                        else:
##                            vitesseY = vehicule.vY+ float(t)
##                            
##                        vehicule.vY = vitesseY + math.sin(vehicule.direction)
##                        vehicule.posX = (vehicule.posX + vehicule.vX + 2*largeur)%(2*largeur)
####                        if(math.fabs(vehicule.posX) > largeur):
####                            if(vehicule.posX > 0):
####                                vehicule.posX %= largeur
####                                vehicule.posX -= largeur
####                            else:
####                                vehicule.posX %= largeur
####                                vehicule.posX = -vehicule.posX
####                                vehicule.posX += largeur
##                        vehicule.posY = (vehicule.posY + vehicule.vY + 2*hauteur)%(2*hauteur)
####                        if(math.fabs(vehicule.posY) > hauteur):
####                            if(vehicule.posY > 0):
####                                vehicule.posY %= hauteur
####                                vehicule.posY -= hauteur
####                            else:
####                                vehicule.posY %= hauteur
####                                vehicule.posY = -vehicule.posY
####                                vehicule.posY += hauteur
##                        vehicule.posX -= largeur
##                        vehicule.posY -= hauteur
                    try:
                        mutexObjectif.acquire()
#                        print("X"+str(objectif.posX))
#                        print("Y"+str(objectif.posY))                            
                        #print("objectif")
                        if((objectif.posX + largeur - vehicule.posX + largeur) < 20.0 and (objectif.posY + hauteur - vehicule.posY + hauteur) < 20.0):
                            vehicule.score +=objectif.valeur;
                            if(vehicule.score >=self.maxScore):
                                print("wiiiiiiiiiiiiiiiiiii") #winner
                                self.winner = True
                            else:
                                print("reset pos")
                                objectif.resetPos()
                                tmpx = "X"+str(objectif.posX)
                                tmpy = "Y"+str(objectif.posY)
                                newObj+=tmpx+tmpy+"/"
                                try:
                                    mutexVehicules.acquire()
                                    for (nom, vehicule) in vehicules.items():
                                        newObj+= nom + ":" + str(vehicule.score) +"|"
                                finally:
                                    mutexVehicules.release()
                                newObj = newObj[:-1]
                                newObj += "/"
                                newObj += "\n"
                        else:
                            print(objectif.posX + largeur - vehicule.posX + largeur)
                            print(objectif.posY + hauteur - vehicule.posY + hauteur)
                    finally:
                        mutexObjectif.release()

                    reponse += str(k)+":"+"X"+str(vehicule.posX)+"Y"+str(vehicule.posY)+"VX"+str(vehicule.vX)+"VY"+str(vehicule.vY)+"T"+str(vehicule.direction)+"|"
#                print(reponse)
                newCommandes = dict()
                reponse = reponse[:-1]
                reponse += "\n"
                for (joueur, s) in self.vehicules.items():
                    #print(reponse)
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
     def __init__(self,host=H,port=P):
        self.sockServ = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        try:
            self.sockServ.bind((host,port))
        except socket.error:
            print("Binding to ",host," at port : ",port, "failed")
            
        self.sockServ.listen(10)
        
        print("------- Server ready --------\n")
        Arena(vehicules).start() 
        while True:
            clientSock, addr = self.sockServ.accept()
            print("------- Client connected ------\n")
            t = Connexion(clientSock,addr);
            t.start()         
server = Server()
