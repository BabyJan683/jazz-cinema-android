# Jazz Cinema — Android App

A YouTube-style movie streaming app that connects **directly to MySQL** — no intermediate server required.

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | XML Layouts + ViewBinding |
| Architecture | MVVM + LiveData |
| Navigation | Fragment Manager + BottomNavigationView |
| Database | MySQL JDBC 5.x (direct connection) |
| Offline Cache | SharedPreferences + Gson (1-hour TTL) |
| Image Loading | Glide 4 |
| Video Player | Media3 ExoPlayer |
| Video URL | Jazz Drive resolver (OkHttp 4) |

## Project Structure

```
app/src/main/java/com/jazzcinema/app/
├── MainActivity.kt                      # 4-tab shell (Home / Search / Library / Me)
├── model/                               # Movie, MovieCategory data classes
├── database/
│   ├── DbConfig.kt                      # MySQL JDBC URL & credentials
│   ├── MovieDao.kt                      # Raw JDBC queries against Movies table
│   └── CategoryBuilder.kt              # Groups flat movies into category rows
├── network/
│   └── JazzDriveResolver.kt            # Resolves Jazz Drive share URL → stream URL
├── repository/
│   └── MovieRepository.kt              # Orchestrates DB + cache + JazzDrive
├── adapter/                             # RecyclerView adapters
├── ui/
│   ├── splash/   SplashActivity         # Cold-start: preloads & caches all movies
│   ├── home/     HomeFragment + VM      # Category rows (horizontal scroll)
│   ├── search/   SearchFragment         # Live search with grid layout
│   ├── library/  LibraryFragment        # Watch history (local, last 50)
│   ├── profile/  ProfileFragment        # App info + DB connection status
│   ├── detail/   MovieDetailActivity    # Thumbnail, title, Play button
│   └── player/   PlayerActivity         # Full-screen ExoPlayer
└── util/         Constants
```

## Database

The app connects directly to:

| Field | Value |
|---|---|
| Host | `sql12.freesqldatabase.com` |
| Database | `sql12824264` |
| Port | `3306` |

Credentials and JDBC URL live in `app/src/main/java/com/jazzcinema/app/database/DbConfig.kt`.

### Expected table schema

```sql
CREATE TABLE Movies (
  id            INT PRIMARY KEY AUTO_INCREMENT,
  title         VARCHAR(255) NOT NULL,
  category      VARCHAR(100) NOT NULL,
  thumbnail_url VARCHAR(500),
  drive_url     VARCHAR(500),
  play_url      VARCHAR(500),
  release_year  INT,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## How Offline Caching Works

1. **Splash screen** opens on every cold start.
2. If a **fresh cache** (< 1 hour old) exists in SharedPreferences → go to main instantly.
3. If cache is **stale or missing** → fetch all rows from MySQL, save as JSON, then go to main.
4. Pull-to-refresh on the Home tab triggers a **forced live fetch** and updates the cache.
5. Search queries always go **live** (never served from cache).

## Building the APK

1. Open Android Studio → **Open** → select the `jazz-cinema-android/` folder
2. Wait for Gradle sync to complete (downloads the Gradle wrapper jar automatically)
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`

For a signed release APK:
- **Build → Generate Signed Bundle / APK**
- Create or use an existing keystore

## Features

- **Home** — movies grouped in horizontal rows (Recently Added, Latest, Bollywood, Hollywood, South…)
- **Search** — live search that queries MySQL directly (min 2 characters)
- **Library** — watch history stored locally (last 50 movies)
- **Me** — app info + DB host + cache freshness status
- **Movie Detail** — thumbnail, title, category, year, Play button
- **Player** — full-screen ExoPlayer, buffering indicator, landscape, Picture-in-Picture
- **Jazz Drive** — share URLs resolved server-side via a 2-step HTTP flow (1-hour in-memory cache)
