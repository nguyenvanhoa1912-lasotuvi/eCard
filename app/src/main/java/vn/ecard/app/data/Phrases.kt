package vn.ecard.app.data

/** Một câu trong thư viện. source: ghi nguồn với câu trích cổ văn / danh ngôn. */
data class Phrase(val id: Int, val occasion: Occasion, val text: Tri, val source: String? = null)

object PhraseLibrary {

    val all: List<Phrase> by lazy { build() }

    fun byOccasion(o: Occasion): List<Phrase> = all.filter { it.occasion == o }
    fun byCategory(c: Category): List<Phrase> = all.filter { it.occasion.category == c }

    private fun build(): List<Phrase> {
        val list = mutableListOf<Phrase>()
        fun p(o: Occasion, vi: String, en: String, zh: String, src: String? = null) {
            list += Phrase(list.size + 1, o, Tri(vi, en, zh), src)
        }

        // ───────────── TẾT NGUYÊN ĐÁN ─────────────
        val t = Occasion.TET
        p(t, "Chúc mừng năm mới! An khang thịnh vượng, vạn sự như ý.",
            "Happy Lunar New Year! Wishing you peace, prosperity and every success.",
            "新年快樂！祝您安康興旺，萬事如意。")
        p(t, "Xuân mới an khang, gia đình hạnh phúc, sức khỏe dồi dào, tài lộc đầy nhà.",
            "May the new spring bring you good health, a happy family and abundant fortune.",
            "新春安康，闔家幸福，身體健康，財源滾滾。")
        p(t, "Cung chúc tân xuân – Phúc lộc song toàn – Năm mới bình an.",
            "Warmest spring greetings – may blessings and fortune come together for you.",
            "恭祝新春，福祿雙全，歲歲平安。")
        p(t, "Chúc năm mới tấn tài tấn lộc, công việc hanh thông, mọi điều suôn sẻ.",
            "Wishing you growing wealth, smooth work and good luck in the new year.",
            "祝您新年進財進祿，事業亨通，一帆風順。")
        p(t, "Năm mới chúc cả nhà sum vầy, ấm no, tiếng cười rộn rã quanh năm.",
            "May your family be together, warm and full of laughter all year round.",
            "新年祝闔家團圓，豐衣足食，笑聲常在。")
        p(t, "Kính chúc ông bà, cha mẹ năm mới mạnh khỏe, sống lâu trăm tuổi.",
            "Wishing our grandparents and parents good health and a long life in the new year.",
            "敬祝爺爺奶奶、爸爸媽媽新年身體健康，長命百歲。")
        p(t, "Chúc Quý đối tác năm mới hợp tác thành công, kinh doanh phát đạt.",
            "Wishing our valued partners a successful year of cooperation and thriving business.",
            "恭祝各位合作夥伴新年合作愉快，生意興隆。")
        p(t, "Tết đến xuân về, chúc bạn vạn sự cát tường, tâm an trí sáng.",
            "As spring arrives, may everything go auspiciously and your heart be at peace.",
            "春回大地，祝您萬事吉祥，心安智明。")
        p(t, "Xuân sang phú quý – Hạ đến bình an – Thu về như ý – Đông tới an khang.",
            "Wealth in spring, peace in summer, wishes fulfilled in autumn and good health in winter.",
            "春來富貴，夏至平安，秋到如意，冬臨安康。")

        // ───────────── NĂM MỚI DƯƠNG LỊCH ─────────────
        val ny = Occasion.NEW_YEAR
        p(ny, "Chúc mừng năm mới! Chúc bạn một năm mới tràn đầy niềm vui và thành công.",
            "Happy New Year! Wishing you a year full of joy and success.",
            "新年快樂！祝您新的一年充滿歡樂與成功。")
        p(ny, "365 ngày mới – 365 cơ hội mới. Chúc bạn biến mọi ước mơ thành hiện thực.",
            "365 new days, 365 new chances. May all your dreams come true.",
            "嶄新的三百六十五天，三百六十五個新機會。願您美夢成真。")
        p(ny, "Năm mới, khởi đầu mới. Chúc bạn sức khỏe, bình an và hạnh phúc.",
            "New year, new beginnings. Wishing you health, peace and happiness.",
            "新年新開始，祝您健康、平安、幸福。")
        p(ny, "Cảm ơn bạn đã đồng hành trong năm qua. Chúc năm mới rực rỡ hơn nữa!",
            "Thank you for being with me this past year. Here's to an even brighter new year!",
            "感謝您過去一年的陪伴，祝新的一年更加燦爛！")
        p(ny, "Chúc gia đình bạn đón năm mới ấm áp, an lành và nhiều may mắn.",
            "Wishing your family a warm, peaceful and lucky new year.",
            "祝您全家溫馨迎新年，平安吉祥，好運連連。")

        // ───────────── SINH NHẬT ─────────────
        val b = Occasion.BIRTHDAY
        p(b, "Chúc mừng sinh nhật! Chúc bạn tuổi mới luôn vui khỏe, thành công và hạnh phúc.",
            "Happy birthday! Wishing you health, success and happiness in the year ahead.",
            "生日快樂！祝您新的一歲健康快樂，事業有成，幸福美滿。")
        p(b, "Tuổi mới thêm nhiều niềm vui, bớt đi muộn phiền, mọi ước mơ đều thành sự thật.",
            "More joy, fewer worries, and may every wish come true this year.",
            "新的一歲，快樂多一些，煩惱少一些，願望都成真。")
        p(b, "Chúc mừng sinh nhật người bạn thân! Cảm ơn vì luôn ở bên mình.",
            "Happy birthday to my dear friend! Thank you for always being there.",
            "祝我的好朋友生日快樂！謝謝你一直在我身邊。")
        p(b, "Con chúc mẹ/ba sinh nhật thật vui, luôn mạnh khỏe và mãi bình an bên con cháu.",
            "Happy birthday, Mom/Dad! May you stay healthy and at peace with your family always.",
            "祝媽媽／爸爸生日快樂，身體健康，永遠平安，兒孫常伴。")
        p(b, "Sinh nhật vui vẻ, em yêu! Mỗi ngày bên em đều là món quà quý giá nhất.",
            "Happy birthday, my love! Every day with you is the most precious gift.",
            "親愛的，生日快樂！與你相伴的每一天都是最珍貴的禮物。")
        p(b, "Chúc bé sinh nhật thật vui, mau ăn chóng lớn, ngoan ngoãn và luôn khỏe mạnh.",
            "Happy birthday, little one! Grow up healthy, happy and kind.",
            "祝小寶貝生日快樂，健康成長，乖巧可愛。")
        p(b, "Chúc mừng sinh nhật anh/chị! Chúc sự nghiệp thăng tiến, gia đình viên mãn.",
            "Happy birthday! Wishing you career growth and a fulfilling family life.",
            "生日快樂！祝您事業步步高升，家庭圓滿。")
        p(b, "Thêm một tuổi, thêm trưởng thành và rạng rỡ. Chúc mừng sinh nhật!",
            "Another year wiser and more radiant. Happy birthday!",
            "又長一歲，更加成熟，更加光彩。生日快樂！")

        // ───────────── MỪNG THỌ ─────────────
        val lo = Occasion.LONGEVITY
        p(lo, "Phúc như Đông Hải – Thọ tỷ Nam Sơn.",
            "May your blessings be as vast as the Eastern Sea and your life as long as the Southern Mountain.",
            "福如東海，壽比南山。", "Thành ngữ cổ")
        p(lo, "Kính chúc ông/bà sống lâu trăm tuổi, mạnh khỏe, vui vầy cùng con cháu.",
            "Wishing you a long, healthy life surrounded by your children and grandchildren.",
            "敬祝您長命百歲，身體安康，兒孫滿堂。")
        p(lo, "Mừng thọ cha mẹ – Chúc tuổi hạc càng cao, phúc đức càng dày.",
            "Celebrating our parents' longevity – may your years and blessings keep growing.",
            "恭祝父母壽辰，鶴壽綿長，福德深厚。")
        p(lo, "Phúc – Lộc – Thọ – Khang – Ninh. Kính chúc ngũ phúc lâm môn.",
            "Blessings, prosperity, longevity, health and peace – may the five blessings come to your home.",
            "福祿壽康寧，恭祝五福臨門。")
        p(lo, "Tùng bách xanh tươi, tuổi hạc vững bền. Kính mừng thượng thọ.",
            "Like evergreen pines, may your years remain strong. Happy longevity celebration.",
            "松柏長青，鶴壽延年。恭賀高壽。")

        // ───────────── ĐÁM CƯỚI ─────────────
        val w = Occasion.WEDDING
        p(w, "Trăm năm hạnh phúc – Bạc đầu giai lão.",
            "A hundred years of happiness – may you grow old together.",
            "百年好合，白頭偕老。", "Thành ngữ cổ")
        p(w, "Chúc hai bạn trăm năm hòa hợp, sớm sinh quý tử, hạnh phúc viên mãn.",
            "Wishing you lifelong harmony, a joyful family and complete happiness.",
            "祝你們百年和合，早生貴子，幸福美滿。")
        p(w, "Chúc mừng hạnh phúc! Chúc tình yêu của hai bạn mãi nồng thắm như ngày đầu.",
            "Congratulations! May your love stay as warm as it is today.",
            "新婚快樂！願你們的愛情永遠如初見般熾熱。")
        p(w, "Tâm đầu ý hợp – Vĩnh kết đồng tâm.",
            "Kindred hearts, united forever.",
            "情投意合，永結同心。", "Thành ngữ cổ")
        p(w, "Chúc cô dâu chú rể cùng nhau xây tổ ấm bình yên, đong đầy yêu thương.",
            "May the bride and groom build a peaceful home filled with love.",
            "祝新郎新娘攜手共築溫馨愛巢，充滿愛意。")
        p(w, "Song hỷ lâm môn – Chúc mừng hai họ.",
            "Double happiness at the door – congratulations to both families.",
            "雙喜臨門，恭賀兩家。")
        p(w, "Từ nay hai bạn là một. Chúc nắm tay nhau đi qua mọi buồn vui của cuộc đời.",
            "From today you are one. May you walk hand in hand through every joy and sorrow.",
            "從今以後，你們合而為一。願你們攜手走過人生的悲歡喜樂。")

        // ───────────── TÂN GIA ─────────────
        val h = Occasion.HOUSEWARMING
        p(h, "Chúc mừng tân gia! An cư lạc nghiệp, gia đạo bình an.",
            "Happy housewarming! May you settle in well and your home be at peace.",
            "恭賀喬遷之喜！安居樂業，家宅平安。")
        p(h, "Nhà mới – Niềm vui mới. Chúc tổ ấm luôn đầy tiếng cười và may mắn.",
            "New home, new joy. May it always be full of laughter and luck.",
            "新居新喜，願您的家充滿歡笑與好運。")
        p(h, "Chúc ngôi nhà mới đón nhiều tài lộc, gia đình hạnh phúc, vạn sự hanh thông.",
            "May your new home bring wealth, family happiness and smooth sailing in everything.",
            "祝新居納福迎財，闔家幸福，萬事亨通。")
        p(h, "Mừng nhà mới khang trang. Chúc gia chủ an khang thịnh vượng.",
            "Congratulations on your beautiful new home. Wishing you health and prosperity.",
            "恭賀新居落成，祝府上安康興旺。")
        p(h, "Kiều thiên chi hỷ – Chúc mừng gia đình đón nhà mới.",
            "Joy of moving to a new home – congratulations to your family.",
            "喬遷之喜，恭賀府上新居。")

        // ───────────── KHAI TRƯƠNG ─────────────
        val op = Occasion.OPENING
        p(op, "Khai trương hồng phát – Vạn sự như ý – Tấn tài tấn lộc.",
            "A prosperous opening – may everything go as you wish and fortune keep growing.",
            "開張大吉，萬事如意，進財進祿。")
        p(op, "Chúc mừng khai trương! Chúc buôn may bán đắt, khách đến đông vui.",
            "Congratulations on your grand opening! Wishing you booming sales and many customers.",
            "恭賀開業！祝生意興隆，顧客盈門。")
        p(op, "Sinh ý hưng long – Tài nguyên quảng tiến.",
            "Thriving business, ever-widening sources of wealth.",
            "生意興隆，財源廣進。", "Thành ngữ cổ")
        p(op, "Chúc doanh nghiệp vững bước phát triển, thành công bền vững.",
            "Wishing your company steady growth and lasting success.",
            "祝貴公司穩步發展，基業長青。")
        p(op, "Hồng phát khai trương – Phát tài phát lộc – Đại cát đại lợi.",
            "A grand opening with abundant fortune, great luck and great profit.",
            "鴻發開張，發財發祿，大吉大利。")

        // ───────────── TỐT NGHIỆP ─────────────
        val g = Occasion.GRADUATION
        p(g, "Chúc mừng tốt nghiệp! Một chặng đường khép lại, một hành trình mới mở ra.",
            "Congratulations, graduate! One chapter closes and a new journey begins.",
            "畢業快樂！一段旅程結束，新的征程開始。")
        p(g, "Tự hào về con! Chúc con vững bước vào đời và thành công rực rỡ.",
            "So proud of you! May you step into the world with confidence and shine.",
            "為你驕傲！祝你踏入社會穩步前行，成就輝煌。")
        p(g, "Chúc bạn tiền đồ rộng mở, sự nghiệp vẻ vang.",
            "Wishing you a bright future and a glorious career.",
            "祝你前程似錦，鵬程萬里。")
        p(g, "Bao năm nỗ lực đã đơm hoa kết trái. Chúc mừng thành quả xứng đáng!",
            "Years of hard work have paid off. Congratulations on a well-deserved achievement!",
            "多年努力終於開花結果，恭喜實至名歸！")

        // ───────────── THĂNG CHỨC ─────────────
        val pr = Occasion.PROMOTION
        p(pr, "Chúc mừng anh/chị thăng chức! Chúc ngày càng thành công trên cương vị mới.",
            "Congratulations on your promotion! Wishing you even greater success in your new role.",
            "恭喜您榮升！祝您在新崗位上更上一層樓。")
        p(pr, "Thăng tiến không ngừng – Sự nghiệp vững vàng.",
            "Continuous advancement and a solid career.",
            "步步高升，事業穩健。")
        p(pr, "Năng lực xứng đáng được ghi nhận. Chúc mừng bước tiến mới!",
            "Your talent truly deserves recognition. Congratulations on this step forward!",
            "實力得到肯定，恭喜邁向新的里程！")
        p(pr, "Chúc sếp sức khỏe, thành công và tiếp tục dẫn dắt đội ngũ vững mạnh.",
            "Wishing you health and success as you continue to lead our team.",
            "祝您身體健康，事業成功，繼續帶領團隊再創佳績。")

        // ───────────── MỪNG BÉ CHÀO ĐỜI ─────────────
        val bb = Occasion.BABY
        p(bb, "Chúc mừng gia đình đón thiên thần nhỏ! Chúc bé khỏe mạnh, hay ăn chóng lớn.",
            "Congratulations on your little angel! Wishing the baby good health and happy growth.",
            "恭喜府上迎來小天使！祝寶寶健康平安，茁壯成長。")
        p(bb, "Mẹ tròn con vuông – Niềm vui nhân đôi.",
            "Mother and baby are well – double the joy.",
            "母子平安，喜上加喜。")
        p(bb, "Chào mừng em bé đến với thế giới. Chúc bé một đời bình an, hạnh phúc.",
            "Welcome to the world, little one. May your life be peaceful and happy.",
            "歡迎小寶貝來到這個世界，願你一生平安喜樂。")
        p(bb, "Chúc ba mẹ mới nhiều sức khỏe để cùng bé lớn lên mỗi ngày.",
            "Wishing the new parents plenty of energy as they grow with their baby.",
            "祝新手爸媽身體健康，陪伴寶寶一天天長大。")

        // ───────────── 8/3 ─────────────
        val wd = Occasion.WOMEN_DAY
        p(wd, "Chúc mừng ngày 8/3! Chúc chị em luôn xinh đẹp, hạnh phúc và tự tin.",
            "Happy Women's Day! May you always be beautiful, happy and confident.",
            "三八婦女節快樂！祝您永遠美麗、幸福、自信。")
        p(wd, "Gửi đến mẹ yêu lời cảm ơn và chúc mẹ luôn mạnh khỏe, vui tươi.",
            "To my dear mother: thank you, and may you always be healthy and cheerful.",
            "獻給親愛的媽媽：感謝您，祝您永遠健康快樂。")
        p(wd, "Chúc các chị em đồng nghiệp ngày 8/3 thật rạng rỡ, công việc thuận lợi.",
            "Wishing all our female colleagues a radiant Women's Day and great success at work.",
            "祝各位女同事婦女節光彩照人，工作順利。")
        p(wd, "Phụ nữ là bông hoa đẹp nhất của cuộc đời. Chúc em luôn hạnh phúc!",
            "Women are the most beautiful flowers of life. Wishing you endless happiness!",
            "女人是生命中最美的花朵，祝你永遠幸福！")

        // ───────────── 20/10 ─────────────
        val vw = Occasion.VN_WOMEN_DAY
        p(vw, "Chúc mừng ngày Phụ nữ Việt Nam 20/10! Chúc luôn xinh đẹp, đảm đang và hạnh phúc.",
            "Happy Vietnamese Women's Day! May you always be beautiful, capable and happy.",
            "越南婦女節快樂！祝您永遠美麗、能幹、幸福。")
        p(vw, "Cảm ơn mẹ, cảm ơn em – những người phụ nữ tuyệt vời nhất của cuộc đời anh.",
            "Thank you, Mom, and thank you, my love – the most wonderful women in my life.",
            "感謝媽媽，感謝你——我生命中最美好的女人。")
        p(vw, "Chúc một nửa thế giới ngày 20/10 ngập tràn hoa và niềm vui.",
            "Wishing the better half of the world a day full of flowers and joy.",
            "祝半邊天的妳們節日充滿鮮花與歡樂。")

        // ───────────── 20/11 ─────────────
        val te = Occasion.TEACHER_DAY
        p(te, "Kính chúc thầy cô nhân ngày Nhà giáo Việt Nam luôn mạnh khỏe, hạnh phúc.",
            "On Vietnamese Teachers' Day, we wish you good health and happiness.",
            "值此越南教師節，敬祝老師身體健康，生活幸福。")
        p(te, "Một chữ cũng là thầy, nửa chữ cũng là thầy. Con xin tri ân công ơn dạy dỗ.",
            "Whoever teaches me even a single word is my teacher. Thank you for all you have taught.",
            "一字為師，半字為師。感謝老師的教誨之恩。", "Thành ngữ")
        p(te, "Cảm ơn thầy cô đã thắp sáng ước mơ và dẫn lối cho chúng em.",
            "Thank you for lighting up our dreams and guiding our way.",
            "感謝老師點亮我們的夢想，為我們指引方向。")
        p(te, "Kính chúc thầy cô dồi dào sức khỏe, mãi là người lái đò tận tụy.",
            "May you stay healthy and remain the devoted guide who carries us across.",
            "敬祝老師身體健康，永遠是辛勤的擺渡人。")
        p(te, "Tôn sư trọng đạo – Ơn thầy nghĩa nặng.",
            "Honouring our teachers – their kindness weighs deep in our hearts.",
            "尊師重道，師恩深重。")

        // ───────────── TRUNG THU ─────────────
        val ma = Occasion.MID_AUTUMN
        p(ma, "Trung thu đoàn viên – Chúc gia đình sum họp, trăng tròn viên mãn.",
            "Happy Mid-Autumn Festival! May your family be reunited under the full moon.",
            "中秋團圓，祝闔家團聚，花好月圓。")
        p(ma, "Chúc các bé đêm trăng rằm vui tươi, rước đèn thật đẹp.",
            "Wishing all the children a joyful full-moon night with beautiful lanterns.",
            "祝小朋友們中秋快樂，提燈賞月樂融融。")
        p(ma, "Chúc người ở xa dù cách trở vẫn cùng ngắm chung một vầng trăng.",
            "Though far apart, may we still share the same bright moon.",
            "但願人長久，千里共嬋娟。", "Tô Thức – Thủy điệu ca đầu (蘇軾《水調歌頭》)")
        p(ma, "Trăng thu tròn đầy, chúc mọi điều tốt đẹp cũng tròn đầy như thế.",
            "As the autumn moon is full, may all good things in your life be just as complete.",
            "秋月圓滿，願您的一切美好也同樣圓滿。")

        // ───────────── GIÁNG SINH ─────────────
        val x = Occasion.CHRISTMAS
        p(x, "Giáng sinh an lành! Chúc bạn một mùa lễ ấm áp bên người thân yêu.",
            "Merry Christmas! Wishing you a warm holiday season with your loved ones.",
            "聖誕快樂！祝您與親人共度溫馨佳節。")
        p(x, "Merry Christmas and Happy New Year – Chúc an vui, hạnh phúc và bình an.",
            "Merry Christmas and a Happy New Year – joy, happiness and peace to you.",
            "聖誕快樂，新年快樂！祝您喜樂、幸福、平安。")
        p(x, "Chúc đêm Noel lung linh, mọi ước nguyện đều thành sự thật.",
            "May your Christmas night sparkle and all your wishes come true.",
            "祝聖誕夜閃閃發光，所有願望都成真。")
        p(x, "Mùa Giáng sinh mang đến yêu thương, chia sẻ và hy vọng. Chúc bạn an lành.",
            "Christmas brings love, sharing and hope. Peace be with you.",
            "聖誕帶來愛、分享與希望，願您平安。")

        // ───────────── VALENTINE ─────────────
        val v = Occasion.VALENTINE
        p(v, "Gửi em – người anh yêu nhất. Chúc mừng ngày lễ tình nhân!",
            "To you – the one I love most. Happy Valentine's Day!",
            "給你——我最愛的人。情人節快樂！")
        p(v, "Cảm ơn vì đã đến bên anh. Mỗi ngày có em đều là Valentine.",
            "Thank you for coming into my life. Every day with you is Valentine's Day.",
            "謝謝你來到我身邊，有你的每一天都是情人節。")
        p(v, "Chấp tử chi thủ, dữ tử giai lão – Nắm tay nhau đi đến cuối cuộc đời.",
            "I hold your hand and we shall grow old together.",
            "執子之手，與子偕老。", "Kinh Thi (《詩經》)")
        p(v, "Yêu là cùng nhau nhìn về một hướng. Chúc tình yêu của chúng ta mãi bền lâu.",
            "Love is looking in the same direction together. May our love last forever.",
            "愛是一起望向同一個方向，願我們的愛天長地久。")

        // ───────────── CẢM ƠN ─────────────
        val th = Occasion.THANKS
        p(th, "Xin chân thành cảm ơn sự giúp đỡ quý báu của anh/chị.",
            "Thank you sincerely for your valuable help.",
            "衷心感謝您的寶貴幫助。")
        p(th, "Cảm ơn Quý khách đã tin tưởng và đồng hành cùng chúng tôi.",
            "Thank you for your trust and for growing with us.",
            "感謝您的信任與一路同行。")
        p(th, "Ơn nghĩa này xin ghi lòng tạc dạ.",
            "Your kindness will remain engraved in my heart.",
            "此恩此德，銘記於心。")
        p(th, "Cảm ơn vì đã luôn ở bên tôi những lúc khó khăn nhất.",
            "Thank you for always being there in my hardest times.",
            "謝謝你在我最困難的時候一直陪伴在我身邊。")

        // ───────────── CHIA BUỒN ─────────────
        val c = Occasion.CONDOLENCE
        p(c, "Vô cùng thương tiếc trước sự ra đi của bác. Xin thành kính phân ưu cùng gia đình.",
            "We are deeply saddened by your loss. Our heartfelt condolences to your family.",
            "驚悉噩耗，不勝悲痛。謹向府上致以深切慰問。")
        p(c, "Thành kính chia buồn cùng gia quyến. Nguyện cầu hương linh sớm về cõi an lành.",
            "Our sincere sympathy to the family. May the departed soul rest in peace.",
            "謹致哀悼，願逝者安息，往生極樂。")
        p(c, "Xin chia buồn cùng anh/chị và gia đình. Mong mọi người sớm vượt qua mất mát này.",
            "Please accept my condolences. I hope you and your family find strength in this time.",
            "節哀順變，願您與家人早日走出傷痛。")
        p(c, "Sự ra đi của người là mất mát lớn lao. Chúng tôi luôn ở bên gia đình lúc này.",
            "Their passing is a great loss. We stand with your family at this time.",
            "斯人已逝，痛失良師益友。此刻我們與府上同在。")
        p(c, "Thay mặt toàn thể công ty, xin gửi lời chia buồn sâu sắc nhất đến gia đình.",
            "On behalf of the entire company, we extend our deepest condolences to your family.",
            "謹代表公司全體同仁，向府上致以最深切的哀悼。")
        p(c, "Nam mô A Di Đà Phật. Nguyện cầu hương linh được siêu sinh tịnh độ.",
            "Namo Amitabha Buddha. May the departed soul be reborn in the Pure Land.",
            "南無阿彌陀佛，願亡者往生淨土。")
        p(c, "Xin hãy giữ gìn sức khỏe. Người đã khuất luôn mong người ở lại được bình an.",
            "Please take care of yourself. Those we lose always wish peace for those who remain.",
            "請保重身體，逝者也希望生者平安。")
        p(c, "Nghĩa tử là nghĩa tận. Xin thắp nén tâm hương tiễn biệt người về miền vĩnh hằng.",
            "With a heartfelt incense, we bid farewell as they journey to eternity.",
            "心香一瓣，送別逝者歸於永恆。")
        p(c, "Hay tin buồn, chúng tôi vô cùng xúc động. Xin thành kính phân ưu.",
            "We were deeply moved to hear the sad news. With sincere sympathy.",
            "驚聞噩耗，深感悲痛，謹致哀思。")
        p(c, "Cầu mong linh hồn được yên nghỉ trong vòng tay Thiên Chúa.",
            "May their soul rest in the arms of God.",
            "願逝者的靈魂在天主懷中安息。")

        // ───────────── TƯỞNG NHỚ / NGÀY GIỖ ─────────────
        val m = Occasion.MEMORIAL
        p(m, "Mãi mãi thương nhớ. Công ơn sinh thành con cháu nguyện ghi khắc.",
            "Forever in our hearts. We will always remember the love that gave us life.",
            "永遠懷念，養育之恩，子孫銘記。")
        p(m, "Uống nước nhớ nguồn – Tưởng nhớ tổ tiên, ông bà.",
            "When drinking water, remember its source – honouring our ancestors.",
            "飲水思源，緬懷祖先。", "Tục ngữ")
        p(m, "Ngày giỗ, cả nhà sum họp thắp nén nhang tưởng nhớ người thân yêu.",
            "On this memorial day, the family gathers to light incense for our beloved.",
            "忌日之時，闔家團聚，焚香追思至親。")
        p(m, "Người đi xa nhưng tình thương ở lại mãi trong tim chúng con.",
            "You have gone far, but your love remains in our hearts forever.",
            "斯人遠去，愛永留心間。")
        p(m, "Thân thể tóc da, nhận từ cha mẹ. Con nguyện sống tốt để báo hiếu.",
            "Our body comes from our parents; I vow to live well to honour them.",
            "身體髮膚，受之父母。願好好生活以盡孝道。", "Hiếu Kinh (《孝經》)")

        // ───────────── MỜI CƯỚI ─────────────
        val iw = Occasion.INVITE_WEDDING
        p(iw, "Trân trọng kính mời Quý khách đến dự tiệc cưới chung vui cùng gia đình chúng tôi.",
            "We cordially invite you to celebrate our wedding with our families.",
            "誠摯邀請您蒞臨我們的婚宴，與兩家同享喜悅。")
        p(iw, "Sự hiện diện của bạn là niềm vinh hạnh cho gia đình chúng tôi.",
            "Your presence would be an honour to our families.",
            "您的光臨將使我們倍感榮幸。")
        p(iw, "Chúng mình sắp về chung một nhà! Rất mong được đón bạn trong ngày vui.",
            "We're getting married! We'd love for you to join us on our big day.",
            "我們要結婚了！誠邀您來分享我們的喜悅。")
        p(iw, "Thời gian: …… Địa điểm: …… Rất hân hạnh được đón tiếp.",
            "Time: …… Venue: …… We look forward to welcoming you.",
            "時間：…… 地點：…… 恭候光臨。")
        p(iw, "Kính mời: …… Đến dự lễ thành hôn của con chúng tôi.",
            "Dear ……, you are invited to the wedding ceremony of our children.",
            "敬邀 …… 光臨小兒（女）婚禮。")

        // ───────────── MỜI DỰ TIỆC / SỰ KIỆN ─────────────
        val ip = Occasion.INVITE_PARTY
        p(ip, "Trân trọng kính mời bạn đến dự tiệc sinh nhật của mình. Có bạn, niềm vui trọn vẹn!",
            "You are warmly invited to my birthday party. It won't be complete without you!",
            "誠摯邀請您參加我的生日派對，有您才完整！")
        p(ip, "Kính mời Quý khách đến dự tiệc mừng tân gia, chung vui cùng gia đình chúng tôi.",
            "We kindly invite you to our housewarming party.",
            "敬邀您蒞臨寒舍，參加喬遷宴。")
        p(ip, "Trân trọng kính mời Quý vị đến dự lễ khai trương. Sự hiện diện của Quý vị là niềm vinh hạnh.",
            "We cordially invite you to our grand opening. Your presence would honour us.",
            "誠邀各位蒞臨開業典禮，您的光臨是我們的榮幸。")
        p(ip, "Kính mời anh/chị tham dự tiệc tất niên, cùng nhìn lại một năm và đón năm mới.",
            "You are invited to our year-end party to look back on the year and welcome the next.",
            "誠邀您參加年終尾牙宴，回顧過去，喜迎新年。")
        p(ip, "Thời gian: …… Địa điểm: …… Rất mong được đón tiếp!",
            "Time: …… Venue: …… We look forward to seeing you!",
            "時間：…… 地點：…… 期待您的光臨！")
        p(ip, "Mời bạn đến họp mặt, ôn chuyện xưa và gắn kết tình thân.",
            "Join our reunion to share old memories and renew our bonds.",
            "邀您共聚一堂，敘舊情，聯情誼。")

        // ───────────── MỜI DỰ GIỖ ─────────────
        val ig = Occasion.INVITE_MEMORIAL
        p(ig, "Gia đình chúng tôi trân trọng kính mời Quý vị đến dự lễ giỗ, thắp nén hương tưởng nhớ.",
            "Our family respectfully invites you to the memorial ceremony to offer incense in remembrance.",
            "敬邀各位出席忌辰祭禮，焚香追思。")
        p(ig, "Nhân ngày giỗ ……, kính mời bà con họ hàng về sum họp.",
            "On the memorial day of ……, we invite our relatives to gather together.",
            "值 …… 忌辰之際，敬邀親友共聚。")
        p(ig, "Thời gian: …… (ngày âm lịch: ……) Địa điểm: …… Kính mời.",
            "Time: …… (lunar date: ……) Venue: …… You are respectfully invited.",
            "時間：……（農曆：……）地點：…… 敬請光臨。")

        // ───────────── LỜI HAY Ý ĐẸP ─────────────
        val q = Occasion.WISDOM
        p(q, "Học mà thường xuyên ôn tập, chẳng phải là điều vui sao?",
            "Is it not a pleasure to learn and to practise what one has learned?",
            "學而時習之，不亦說乎？", "Luận Ngữ (《論語》)")
        p(q, "Có bạn từ phương xa đến, chẳng phải là điều vui sao?",
            "Is it not a joy to have friends come from afar?",
            "有朋自遠方來，不亦樂乎？", "Luận Ngữ (《論語》)")
        p(q, "Điều mình không muốn, chớ làm cho người khác.",
            "Do not do to others what you would not want done to yourself.",
            "己所不欲，勿施於人。", "Luận Ngữ (《論語》)")
        p(q, "Hành trình ngàn dặm bắt đầu từ một bước chân.",
            "A journey of a thousand miles begins with a single step.",
            "千里之行，始於足下。", "Lão Tử – Đạo Đức Kinh (《道德經》)")
        p(q, "Người hiểu người khác là trí, người hiểu chính mình là sáng.",
            "Knowing others is wisdom; knowing yourself is enlightenment.",
            "知人者智，自知者明。", "Lão Tử – Đạo Đức Kinh (《道德經》)")
        p(q, "Điều thiện cao nhất giống như nước.",
            "The highest good is like water.",
            "上善若水。", "Lão Tử – Đạo Đức Kinh (《道德經》)")
        p(q, "Có công mài sắt, có ngày nên kim.",
            "With persistence, an iron rod can be ground into a needle.",
            "只要功夫深，鐵杵磨成針。", "Tục ngữ")
        p(q, "Thất bại là mẹ thành công.",
            "Failure is the mother of success.",
            "失敗乃成功之母。", "Tục ngữ")
        p(q, "Một cây làm chẳng nên non, ba cây chụm lại nên hòn núi cao.",
            "One tree cannot make a mountain; three trees together can.",
            "獨木不成林，眾志可成城。", "Ca dao")
        p(q, "Lá lành đùm lá rách.",
            "The whole leaf wraps the torn one – those who have help those in need.",
            "好葉包破葉，守望相助。", "Tục ngữ")
        p(q, "Ăn quả nhớ kẻ trồng cây.",
            "When eating the fruit, remember who planted the tree.",
            "吃果子，拜樹頭。", "Tục ngữ")
        p(q, "Đi một ngày đàng, học một sàng khôn.",
            "Travel one day, gain a basket of wisdom.",
            "行萬里路，勝讀萬卷書。", "Tục ngữ")
        p(q, "Gia hòa vạn sự hưng.",
            "When the family is in harmony, all things prosper.",
            "家和萬事興。", "Thành ngữ")
        p(q, "Tri túc thường lạc – Biết đủ thì luôn vui.",
            "Contentment brings lasting happiness.",
            "知足常樂。", "Thành ngữ")
        p(q, "Hãy sống mỗi ngày như thể đó là một món quà.",
            "Live each day as if it were a gift.",
            "把每一天都當作一份禮物來珍惜。")
        p(q, "Nụ cười là ngôn ngữ chung của mọi trái tim.",
            "A smile is the universal language of every heart.",
            "微笑是所有心靈共通的語言。")
        p(q, "Lòng biết ơn biến những gì ta có thành đủ đầy.",
            "Gratitude turns what we have into enough.",
            "感恩讓我們所擁有的變得富足。")
        p(q, "Tâm an thì vạn sự an.",
            "When the heart is at peace, all things are at peace.",
            "心安即是歸處，心安萬事安。")
        p(q, "Nước chảy đá mòn – Kiên trì sẽ thành công.",
            "Dripping water wears away stone – persistence brings success.",
            "水滴石穿，堅持必成。", "Thành ngữ")
        p(q, "Hôm nay cố gắng một chút, ngày mai rạng rỡ nhiều hơn.",
            "A little more effort today, a lot more shine tomorrow.",
            "今天多努力一點，明天更加燦爛。")

        return list
    }
}
