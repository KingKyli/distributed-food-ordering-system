# 🚀 Οδηγίες για το Run Button στο Android Studio

## ✅ Έχω φτιάξει τα configurations!

Τώρα μπορείς να χρησιμοποιήσεις το **Run button** (▶️) στο Android Studio!

## 📋 Βήματα για να τρέξεις την εφαρμογή:

### Μέθοδος 1: Αυτόματη (Συνιστάται)

1. **Ξεκίνα τον emulator πρώτα:**
   ```powershell
   .\start-emulator.ps1
   ```
   
2. **Ξεκίνα τον server (σε ξεχωριστό terminal):**
   ```powershell
   .\start-server.ps1
   ```
   
3. **Στο Android Studio:**
   - Πάτα το **Run button** (▶️) ή `Shift+F10`
   - Θα πρέπει να δεις τον emulator στο dropdown
   - Αν δεν τον βλέπεις, πάτα το dropdown δίπλα στο Run και επέλεξε `Medium_Phone_API_36.0`

### Μέθοδος 2: Ολοκληρωμένη με ένα script

```powershell
.\quick-start.ps1 -SkipBuild
```

Αυτό ξεκινάει τα πάντα αυτόματα.

## 🔧 Τι έγινε για να δουλέψει το Run:

### 1. ✅ Δημιούργησα run configuration
- Αρχείο: `.idea/runConfigurations/app.xml`
- Ρυθμίσεις: Αυτόματο logcat, preferred AVD

### 2. ✅ Ενημέρωσα το deployment target
- Αρχείο: `.idea/deploymentTargetSelector.xml`  
- Αλλαγή: Από physical device σε emulator `Medium_Phone_API_36.0`

### 3. ✅ Ενημέρωσα το workspace
- Αρχείο: `.idea/workspace.xml`
- Ενεργοποίηση: Auto-show logcat

## 🎯 Τι να περιμένεις όταν πατάς Run:

1. **Build**: Το Android Studio θα κάνει build το APK
2. **Install**: Θα το εγκαταστήσει στον emulator
3. **Launch**: Θα ανοίξει την εφαρμογή αυτόματα
4. **Logcat**: Θα ανοίξει αυτόματα το logcat window

## ⚠️ Αν δεν δουλεύει:

### Πρόβλημα 1: Δεν βλέπω emulator στο dropdown

**Λύση:**
```powershell
# Ξεκίνα τον emulator πρώτα
.\start-emulator.ps1
```

Μετά στο Android Studio:
- Tools → Device Manager
- Θα πρέπει να βλέπεις το `Medium_Phone_API_36.0` ως "Running"

### Πρόβλημα 2: "No target device found"

**Λύση:**
```powershell
# Έλεγξε αν τρέχει
adb devices

# Αν δεν τρέχει, ξεκίνα τον
.\start-emulator.ps1
```

### Πρόβλημα 3: Build fails με "Emulator not found"

**Λύση:**
- Πάτα το dropdown δίπλα στο Run button
- Επέλεξε "Medium_Phone_API_36.0"
- Πάτα Run ξανά

### Πρόβλημα 4: App crashes on launch

**Λύση:**
```powershell
# Βεβαιώσου ότι τρέχει ο server
.\start-server.ps1
```

## 📱 Device Manager

Μπορείς να διαχειριστείς τον emulator από το Android Studio:

1. **Άνοιξε Device Manager:**
   - Κλικ στο icon με τα 3 τετράγωνα (συνήθως στην πάνω δεξιά γωνία)
   - Ή: Tools → Device Manager

2. **Ξεκίνα/Σταμάτα emulator:**
   - Δες τον `Medium_Phone_API_36.0`
   - Πάτα το ▶️ για start ή ⏹️ για stop

## 🎮 Shortcuts

- **Run**: `Shift + F10`
- **Debug**: `Shift + F9`
- **Stop**: `Ctrl + F2`
- **Build**: `Ctrl + F9`
- **Select Device**: `Alt + Shift + F10` → επέλεξε device

## 💡 Tips

### Για γρήγορο development:

1. **Άφησε τον emulator ανοιχτό** - δεν χρειάζεται να τον κλείνεις
2. **Άφησε τον server να τρέχει** - σε background terminal
3. **Χρησιμοποίησε instant run** - αλλαγές χωρίς reinstall

### Για performance:

- Ο emulator ρυθμίζεται στα 2GB RAM (για να μην κρασάρει)
- Αν έχεις θέμα με memory, κλείσε άλλα programs

## 🔄 Workflow

Συνιστώμενο workflow:

```powershell
# Μία φορά το πρωί:
.\start-emulator.ps1
.\start-server.ps1

# Μετά στο Android Studio:
# Πάτα Run (▶️) όσες φορές θες!
```

## 📊 Status Check

Για να δεις τι τρέχει:

```powershell
# Emulator
adb devices

# Server  
netstat -an | Select-String "8765"

# App
adb shell dumpsys window | Select-String "mCurrentFocus"
```

## 🎉 Τελικό Test

Για να δοκιμάσεις ότι όλα δουλεύουν:

1. Άνοιξε Android Studio
2. Πάτα Run (▶️)
3. Αν είδες dialog για device selection → επέλεξε `Medium_Phone_API_36.0`
4. Πάτα OK
5. Περίμενε build & install
6. Η app θα ανοίξει αυτόματα!

---

**Σημείωση:** Αν χρειάζεσαι βοήθεια, τσέκαρε το `SCRIPTS.md` για troubleshooting!

