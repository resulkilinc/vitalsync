# VitalÖlçüm (vitalsync)

Kotlin / Jetpack Compose Android uygulaması. Yerel olarak vital bulguları (tansiyon, glikoz, nabız, SpO₂) kaydeder, klinik kurallarla analiz eder, grafik ve PDF rapor üretir.

> Teknik proje adı: `vitalsync` · Cihazda görünen ad: **VitalÖlçüm**

## Özellikler

- Çok adımlı kayıt ve PIN ile giriş (PBKDF2-HMAC-SHA256)
- Room ile yerel veri (kullanıcı, vital kayıtları, hatırlatmalar, sync outbox)
- MVVM + Repository + Hilt
- `HealthAnalysisEngine` ile deterministik klinik analiz
- Grafikler, hatırlatmalar, WorkManager senkron (demo REST: JSONPlaceholder)
- Yerel PDF rapor üretimi ve paylaşım

## Gereksinimler

- Android Studio (AGP 8.7.x ile uyumlu sürüm)
- JDK 17
- Android SDK Platform 35

## Kurulum

1. Bu deposunu klonla.
2. Android Studio’da proje kökünü aç (bu klasör).
3. Gradle sync’in bitmesini bekle.
4. `app` modülünü seçip çalıştır.

```bash
./gradlew :app:installDebug
```

`local.properties` Android Studio tarafından otomatik oluşturulur; repoya eklenmez.

## Mimari (kısa)

```
app/src/main/java/com/vitalsync/app/
├── data/          # Room, repository, remote, security
├── domain/        # model, HealthAnalysisEngine, klinik kaynaklar, PDF
├── presentation/  # Compose ekranları, ViewModel’ler, tema
├── sync/          # Outbox + WorkManager
├── di/            # Hilt modülleri
└── util/          # yedekleme, bildirim, hatırlatma worker
```

## Notlar

- Ağ katmanı demo amaçlıdır (`jsonplaceholder.typicode.com`).
- Sağlık verisi cihazda tutulur; bu depo yalnızca kaynak kod içerir.
- `local.properties`, build çıktıları ve IDE dosyaları `.gitignore` ile dışarıda bırakılmıştır.
