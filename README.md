# timeboard-demo
🚀 Présentation Projet – TimeBoard (CI/CD & DevSecOps)
1️⃣ Contexte & Objectif

j’ai construit un mini-projet autour d’une mini application Java, afin de démontrer ma capacité à industrialiser un produit similaire à e-Temptation : build, qualité, sécurité, packaging, déploiement et supervision via CI/CD.

Mon objectif n’était pas de faire une grosse application métier, mais de créer une chaîne de production logicielle fiable et automatisée, comme celle que vous attendez pour vos équipes R&D.

2️⃣ L’application TimeBoard

L’application s’appelle TimeBoard.

C’est une petite application Java Spring Boot.

Elle affiche une vue de gestion des temps pour un utilisateur : jours, projets, heures travaillées.

Elle expose aussi une API de santé : /health utilisée par la CI/CD.

🧩 Important :
L’application est packagée en JAR, puis intégrée dans une image Docker, et exécutée dans un conteneur Docker sur une VM EC2.
👉 Ce n’est pas un .exe ou un .msi, mais le principe de build et de packaging est le même : produire un artefact exécutable, prêt à être livré et redéployé de façon standardisée.

3️⃣ Pipeline CI/CD – Jenkins

Autour de cette application, j’ai construit une chaîne CI/CD Jenkins structurée en plusieurs étapes :

🔹 1. Récupération du code

Jenkins récupère le code depuis un dépôt Git (GitHub, mais transposable à GitLab).

🔹 2. Build & Qualité

Maven :

Compilation du code Java

Exécution des tests unitaires

Packaging en JAR exécutable
→ timeboard-demo-1.0.0.jar

🔹 3. Sécurité & DevSecOps

Intégration de contrôles de sécurité légers mais concrets :

Gitleaks : scan des secrets dans le code

OWASP Dependency-Check : analyse des dépendances Java pour détecter les CVE

SpotBugs : analyse statique (SAST) simple côté Java

Trivy : scan de vulnérabilités sur l’image Docker construite

Conftest + Rego : contrôle de règles basiques sur la configuration de déploiement
(par exemple : pas d’image taggée latest, port conteneur standardisé)

Ces étapes permettent d’avoir des rapports de sécurité automatisés, sans bloquer la livraison, mais en donnant aux équipes R&D une visibilité claire sur les risques.

🔹 4. Packaging & Image Docker

Build d’une image Docker contenant :

le runtime Java

le JAR de l’application

L’image est taggée de manière versionnée :
ngueyepmodeste/timeboard:<numéro de build>

🔹 5. Déploiement automatisé

Jenkins se connecte en SSH sur le serveur applicatif (EC2).

Il :

arrête le conteneur précédent

récupère l’image depuis Nexus

lance le nouveau conteneur avec :
docker run -d -p 80:8080 …

🔹 6. Healthcheck post-déploiement

Après déploiement, Jenkins effectue un healthcheck HTTP sur /health, exécuté directement sur le serveur (via SSH).

Un mécanisme de retry est en place pour tenir compte du temps de démarrage de Java.

👉 Au final, chaque commit peut conduire à une nouvelle version buildée, scannée, stockée, déployée et vérifiée automatiquement.

4️⃣ Outils alignés avec votre stack

L’offre mentionne : GitLab, Jenkins, Git, Docker, Nexus, Windows & Linux.

Voici comment mon projet s’aligne :

Git / GitLab :
Le projet est versionné dans Git ; la même logique s’applique à GitLab CI ou à des hooks GitLab → Jenkins.

Jenkins :
C’est le cœur de la chaîne CI/CD, avec un Jenkinsfile déclaratif.

Docker :
L’application est exécutée dans un conteneur Docker, comme on le ferait pour des environnements de test ou pré-prod.

Nexus :
J’utilise Nexus comme registre central pour :

les images Docker

les artefacts (JAR, rapports de sécurité, etc.)

Linux :
Les serveurs CI/CD et applicatif sont des VM Linux (Ubuntu).
Les mêmes principes sont applicables sur des VM Windows ou VMWare on-premise.

5️⃣ Infrastructure d’exécution (AWS + Docker)

Pour simuler un environnement éditeur logiciel / client final, j’ai déployé cette chaîne sur une infra simple :

🖥️ EC2 #1 – CI/CD & Registry (ci-nexus-jenkins)

OS : Ubuntu

Rôles :

Jenkins : orchestre la chaîne CI/CD

Nexus : centralise artefacts et images Docker

Outils DevSecOps : gitleaks, trivy, conftest…

Ports :

8080 → interface Jenkins (restreinte à mon IP)

8081 → interface Nexus

8083 → registry Docker Nexus

🖥️ EC2 #2 – Serveur applicatif (app-server)

OS : Ubuntu

Rôle :

héberge le conteneur Docker de l’application TimeBoard

Port :

80 → exposé pour accéder à l’application depuis l’extérieur

🔎 Précision importante :
Ce n’est pas une installation de type setup .exe ou .msi comme vous pouvez le faire chez certains clients, mais le principe est analogue :

chez vous : livrer un binaire installable + scripts / procédures

dans mon projet : livrer une image Docker exécutable prête à être tirée et lancée sur un serveur

6️⃣ Rôle de Nexus & répartition des artefacts

J’ai fait en sorte que tous les artefacts importants soient centralisés et nommés clairement dans Nexus.

🔹 1. Registry Docker (repo docker-hosted)

Stocke les images de l’application :

ip_nexus:8083/ngueyepmodeste/timeboard:<build>

Chaque build Jenkins produit une nouvelle image versionnée.

Ces images sont utilisées par le serveur applicatif pour déployer la bonne version.

🔹 2. Repository Raw (ex : timeboard-artifacts)

Ce repo stocke :

le JAR de l’application
→ ex : timeboard-demo-1.0.0.jar

les rapports de sécurité et qualité :

gitleaks-report.json

dependency-check-report.html

spotbugsXml.xml

trivy-report.txt

conftest-report.txt

👉 Résultat :

Traçabilité complète par build

Possibilité de revenir à une version précise

Facilite les audits, l’analyse de sécurité, les investigations R&D

C’est exactement la logique qu’on attend d’une chaîne de production logicielle pour un produit d’éditeur.
