package com.vitalsync.app.presentation.guide

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.presentation.theme.*

data class HealthGuideCategory(
    val emoji: String,
    val title: String,
    val cards: List<HealthGuideCard>
)

data class HealthGuideCard(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthGuideScreen(
    onBack: () -> Unit
) {
    val categories = remember { getHealthGuideData() }
    var expandedCardIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
            }
            Text(
                "Sağlık Rehberi",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(12.dp))

        // Başlık kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📚", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Sağlık Bilgi Kartları",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Ölçümlerinizin ne anlama geldiğini öğrenin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Kategoriler
        categories.forEachIndexed { catIndex, category ->
            Text(
                "${category.emoji} ${category.title}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            category.cards.forEachIndexed { cardIndex, card ->
                val isExpanded = expandedCardIndex == Pair(catIndex, cardIndex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        expandedCardIndex = if (isExpanded) null else Pair(catIndex, cardIndex)
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                card.question,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (isExpanded) "▲" else "▼",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    card.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Text(
                "⚕️ Bu bilgiler genel sağlık eğitimi amaçlıdır.\nTıbbi tanı ve tedavi yerine geçmez. Doktorunuza danışın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun getHealthGuideData(): List<HealthGuideCategory> = listOf(
    HealthGuideCategory(
        emoji = "🩺",
        title = "Kan Basıncı (Tansiyon)",
        cards = listOf(
            HealthGuideCard(
                question = "Kan basıncı nedir?",
                answer = "Kan basıncı, kalbin pompaladığı kanın damar duvarlarına uyguladığı basınçtır. İki değerle ölçülür:\n\n• Sistolik (büyük tansiyon): Kalp kasıldığında oluşan basınç\n• Diyastolik (küçük tansiyon): Kalp gevşediğinde damar içindeki basınç\n\nÖrneğin 120/80 mmHg değeri, sistolik 120 ve diyastolik 80 demektir."
            ),
            HealthGuideCard(
                question = "Normal kan basıncı değerleri nelerdir?",
                answer = "Bu uygulama bilgilendirme amaçlıdır; tanı ve tedavi hedefleri yalnızca hekiminizle belirlenir.\n\nESC 2024 çerçevesinde ofis ölçümü için sık kullanılan bantlar:\n\n• Yükselmiş değil (non-elevated): sistolik <120 ve diyastolik <70 mmHg\n• Yükselmiş (elevated): sistolik 120–139 veya diyastolik 70–89 mmHg\n• Hipertansiyon ile uyumlu sınıflama: sistolik ≥140 veya diyastolik ≥90 mmHg\n\nEv ölçümü (HBPM) ortalaması için birçok kılavuzda tanı/ risk değerlendirmesinde ≥135/85 eşiği anılır; tek ölçüm yerine birkaç günün ortalaması önemlidir."
            ),
            HealthGuideCard(
                question = "Tansiyonu nasıl doğru ölçmeliyim?",
                answer = "Doğru ölçüm için şu kurallara dikkat edin:\n\n1. Ölçümden 30 dakika önce kafein, alkol ve sigara kullanmayın\n2. 5 dakika dinlendikten sonra ölçün\n3. Sırtınızı dayayarak oturun, ayaklarınız yerde olsun\n4. Manşeti çıplak kola, kalp hizasına sarın\n5. Ölçüm sırasında konuşmayın\n6. İdeal olarak sabah ve akşam, aynı saatlerde ölçün"
            ),
            HealthGuideCard(
                question = "Hipertansiyon tedavi edilmezse ne olur?",
                answer = "Kontrolsüz yüksek tansiyon zamanla ciddi organ hasarlarına yol açabilir:\n\n• Kalp krizi ve kalp yetmezliği\n• İnme (felç)\n• Böbrek yetmezliği\n• Görme kaybı\n• Periferik arter hastalığı\n\nBu nedenle düzenli takip ve gerektiğinde tedavi çok önemlidir."
            )
        )
    ),
    HealthGuideCategory(
        emoji = "🩸",
        title = "Kan Şekeri (Glukoz)",
        cards = listOf(
            HealthGuideCard(
                question = "Kan şekeri nedir?",
                answer = "Kan şekeri (glukoz), kanınızdaki şeker miktarıdır. Vücudunuzun temel enerji kaynağıdır. Yediklerinizden gelen karbonhidratlar glukoza dönüştürülür ve kana karışır.\n\nİnsülin hormonu, bu şekerin hücrelere girmesini sağlar. İnsülin yetersizliği veya direnci diyabete yol açar."
            ),
            HealthGuideCard(
                question = "Normal kan şekeri değerleri nelerdir?",
                answer = "Tanı yalnızca laboratuvar ve hekim değerlendirmesiyle konur; tek ölçüm yeterli değildir.\n\nDiyabetsiz yetişkinlerde sık anılan laboratuvar eşikleri (ADA ile uyumlu çerçeve):\n\nAçlık (en az 8 saat):\n• <100 mg/dL ile uyumlu olabilir\n• 100–125 mg/dL: prediyabet aralığı ile uyumlu olabilir\n• ≥126 mg/dL: diyabet tanı eşiği ile uyumlu olabilir (doğrulama gerekir)\n\nOGTT 2. saat:\n• <140 mg/dL\n• 140–199 mg/dL: bozulmuş glukoz toleransı aralığı ile uyumlu olabilir\n• ≥200 mg/dL: diyabet eşiği ile uyumlu olabilir\n\nDiyabet tanısı almış kişilerde hedefler kişiselleşir; Türkiye’de sıkça TEMD kılavuzları (ör. açlık/öğün öncesi 80–130 mg/dL bandı, 2. saat toklukta <160 mg/dL çizgisi) referans alınır; ADA’da çoğu yetişkin için tokluk sonrası <180 mg/dL hedefi de anılır."
            ),
            HealthGuideCard(
                question = "Hipoglisemi (düşük şeker) belirtileri nelerdir?",
                answer = "Kan şekeri 70 mg/dL altına düştüğünde aşağıdaki belirtiler görülebilir:\n\n• Titreme, terleme\n• Çarpıntı\n• Açlık hissi\n• Baş dönmesi\n• Bulanık görme\n• Konsantrasyon güçlüğü\n• Çok düşük seviyelerde bilinç kaybı\n\nAcil durumda hızlı emilen şeker (meyve suyu, şekerli su) tüketin."
            )
        )
    ),
    HealthGuideCategory(
        emoji = "❤️",
        title = "Nabız (Kalp Atım Hızı)",
        cards = listOf(
            HealthGuideCard(
                question = "Nabız nedir?",
                answer = "Nabız, kalbin dakikada kaç kez attığını gösteren vital bir bulgudur. Kalp her kasıldığında damarlarda hissedilen atımdır.\n\nNabız; yaş, fiziksel aktivite, stres, ilaçlar ve genel sağlık durumundan etkilenir."
            ),
            HealthGuideCard(
                question = "Normal nabız değerleri nelerdir?",
                answer = "Yetişkinlerde istirahat halinde normal nabız:\n\n• Normal aralık: 60-100 bpm\n• Sporcular: 40-60 bpm (normal kabul edilir)\n• Taşikardi (hızlı): > 100 bpm\n• Bradikardi (yavaş): < 60 bpm\n\nKardiyak sağlık için ideal istirahat nabzı genellikle 60-80 bpm arasındadır."
            ),
            HealthGuideCard(
                question = "Nabız düzensizliği (aritmi) ne demektir?",
                answer = "Kalp atımının düzensiz, çok hızlı veya çok yavaş olması aritmi olarak adlandırılır.\n\nBelirtiler:\n• Çarpıntı hissi\n• Göğüste çırpınma\n• Baş dönmesi\n• Nefes darlığı\n• Bayılma\n\nSık veya şiddetli aritmi belirtilerinde mutlaka bir kardiyoloji uzmanına başvurun."
            )
        )
    ),
    HealthGuideCategory(
        emoji = "🫁",
        title = "Oksijen Saturasyonu (SpO₂)",
        cards = listOf(
            HealthGuideCard(
                question = "SpO₂ nedir?",
                answer = "SpO₂ (Periferik Oksijen Satürasyonu), kanınızdaki hemoglobinin ne kadarının oksijenle dolu olduğunu yüzde olarak gösterir.\n\nPuls oksimetre cihazı ile parmak ucundan ölçülür. Akciğerlerinizin ve dolaşım sisteminizin ne kadar iyi çalıştığını yansıtır."
            ),
            HealthGuideCard(
                question = "Normal SpO₂ değerleri nelerdir?",
                answer = "Genel yetişkin bilgilendirmesi (kişisel hedefinizi mutlaka doktorunuzla netleştirin):\n\n• Çoğu sağlıklı kişide sık görülen bant: %95–100\n• %93–94: tekrar ölçüm ve ölçüm kalitesi; semptom varsa değerlendirme\n• ≤%92: sağlık profesyoneli ile temas önerilir\n• ≤%88 veya nefes darlığı/göğüs ağrısı/morarma/bilinç bulanıklığı: acil değerlendirme (112) düşünülmelidir\n\nKOAH veya hiperkapni riskinde hedef SpO₂ kişisel olabilir (çoğu zaman doktorun belirlediği %88–92 bandı); bu “herkes için normal” anlamına gelmez.\n\nSoğuk parmak, oje/takma tırnak, hareket ve cihaz kalitesi okumayı etkileyebilir (FDA uyarıları). Yüksek rakımda değer düşebilir; semptom varsa rakıma güvenmeyin."
            ),
            HealthGuideCard(
                question = "SpO₂ düştüğünde ne yapmalıyım?",
                answer = "1. Sakin olun; parmak ılık olsun, hareketsiz ölçün.\n2. Okuma sabitlenene kadar bekleyip tekrar ölçün (düşük değerde çift doğrulama).\n3. %93–94 bandında semptomsuzken bile tekrarlayan düşüşlerde doktorunuza bildirin.\n4. ≤%92 veya belirgin nefes darlığı: kısa sürede sağlık profesyoneline ulaşın.\n5. ≤%88 veya ciddi solunum sıkıntısı, göğüs ağrısı, bilinç değişikliği: 112/acil düşünün."
            )
        )
    )
)
