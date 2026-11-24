# Guide de démonstration et de soutenance

Ce document sert de fil conducteur pour présenter **AgriSouk** en 8 à 10 minutes et pour
répondre aux questions techniques les plus probables.

---

## 1. Avant de commencer

- Installer `livrables/AgriSouk-debug.apk` sur le téléphone (ou lancer l'émulateur).
- Vérifier que le téléphone a **Internet** (météo + portail).
- Comptes prêts : `admin@agrisouk.tn / admin123` et `user@agrisouk.tn / user123`.
- Ouvrir l'application une première fois pour que les données de démonstration se créent.

---

## 2. Scénario de démonstration (pas à pas)

### Étape 1 — Connexion et rôle (1 min)
1. Lancer l'app : **écran de connexion**.
2. Cliquer sur **Admin**, puis **Se connecter**.
3. Montrer l'**accueil administrateur** : salutation, badge « Administrateur »,
   statistiques (produits, catégories, favoris), météo du jour.

> À dire : « Deux rôles coexistent. L'administrateur gère le catalogue ; l'utilisateur
> consulte et suit ses favoris. »

### Étape 2 — Marché et vues complexes (2 min)
1. Onglet **Marché** : montrer la **ListView** avec `BaseAdapter`/`ViewHolder`.
2. Taper `tom` dans la recherche → la liste se filtre instantanément.
3. Choisir la catégorie **Fruits** → 4 produits.
4. Trier par **Prix croissant** → l'ordre change.
5. Revenir à « Toutes les catégories », tri « Nom (A-Z) ».

> À dire : « Le `ViewHolder` évite de refaire les `findViewById` à chaque ligne, et
> `convertView` recycle les vues : c'est le mécanisme demandé dans le cours. »

### Étape 3 — Fiche produit et graphique (1 min 30)
1. Ouvrir **Tomate**.
2. Montrer le **graphique 7 jours** (vue personnalisée dessinée au `Canvas`), avec
   Min / Moyenne / Max.
3. Cliquer sur **Ajouter aux favoris** → le bouton devient « Retirer des favoris ».
4. Montrer le bouton **Partager** (menu de partage Android).

### Étape 4 — Météo via API (1 min)
1. Onglet **Météo** : conditions actuelles + **prévisions 5 jours**.
2. Montrer le **conseil agricole** et la carte **Source des données** (Open-Meteo).
3. Glisser vers le bas pour rafraîchir.

> À dire : « Les données viennent de l'API publique Open-Meteo en HTTPS. Elles sont mises
> en cache : si le réseau tombe, le dernier relevé reste affiché. »

### Étape 5 — Bulletin WebView (1 min)
1. Menu (⋮) → **Bulletin du marché** : page **HTML locale** générée depuis SQLite,
   regroupée par catégorie.
2. Cliquer sur **Portail Agriculture** : chargement du site **agriculture.tn**.
3. Revenir sur **Bulletin local** pour montrer la bascule instantanée.

> À dire : « La WebView sert à deux choses : afficher un bulletin HTML construit à partir
> de la base, et ouvrir un vrai site externe. »

### Étape 6 — Administration CRUD (2 min)
1. Onglet **Marché** → bouton flottant **+** (visible seulement pour l'admin).
2. **Ajouter** un produit : nom, catégorie, unité, prix. La **référence se génère toute
   seule** (ex. `TOM-001`).
3. **Modifier** un produit (icône crayon) : changer le prix → enregistrer.
4. **Supprimer** (icône corbeille) → boîte de confirmation → « Supprimer ».
5. Montrer que le compteur « N produits au catalogue » se met à jour.

### Étape 7 — Rôle utilisateur (1 min)
1. Profil → **Se déconnecter**.
2. Connexion **Utilisateur**.
3. Montrer : pas de bouton d'ajout, pas de ligne « Gestion des produits », accueil avec
   raccourci **Favoris**.

> À dire : « L'interface s'adapte au rôle, et l'écran d'administration est en plus protégé
> au niveau du manifeste (non exporté) et du code (contrôle de session). »

---

## 3. Questions probables et réponses courtes

**Pourquoi une `ListView` et pas une `RecyclerView` ?**
> Le cours porte sur les vues complexes classiques. `ListView` + `BaseAdapter` +
> `ViewHolder` illustre directement le recyclage des vues.

**Comment fonctionne le `ViewHolder` ?**
> Le `ViewHolder` garde les références des vues d'une ligne dans le `tag` de `convertView`.
> Au recyclage, on réutilise ces références au lieu de rappeler `findViewById`.

**Où sont stockées les données ?**
> Dans une base **SQLite** (`DatabaseHelper`) : utilisateurs, produits, historique des prix
> et favoris.

**Les mots de passe sont-ils sécurisés ?**
> Ils sont hachés en **SHA-256** et jamais stockés en clair. Pour de la production, on
> ajouterait un **sel** par utilisateur ou une fonction lente (bcrypt/PBKDF2/Argon2) ; ici
> le but est pédagogique.

**Comment l'API est-elle appelée ?**
> `HttpURLConnection` en HTTPS sur un `ExecutorService`, réponse JSON lue avec `org.json`,
> puis retour au thread principal via un `Handler`.

**Que se passe-t-il sans Internet ?**
> La météo s'affiche depuis le cache et un message l'indique ; le bulletin local et le
> catalogue fonctionnent entièrement hors ligne.

**Comment les fragments conservent-ils leur état ?**
> On utilise `show()`/`hide()` plutôt que `replace()` : les vues restent en mémoire, donc
> la position de liste et les filtres sont conservés.

**Comment la WebView est-elle sécurisée ?**
> Le JavaScript est activé uniquement pour nos pages, le chargement reste dans
> `WebViewActivity`, et le bouton retour navigue dans l'historique avant de fermer l'écran.

**Comment le graphique est-il dessiné ?**
> `PriceChartView` étend `View`, redéfinit `onDraw()` et trace grille, aire dégradée, ligne
> et points avec `Paint` et `Path`.

---

## 4. Checklist du jour J

- [ ] APK installé et testé sur le téléphone (mode avion testé pour le cache météo).
- [ ] Les deux comptes fonctionnent.
- [ ] Le dépôt GitHub est public et à jour (`git push`).
- [ ] L'historique de commits est visible sur GitHub.
- [ ] Captures d'écran disponibles dans `captures/`.
- [ ] Déclaration de l'assistance IA présente dans le `README`.
