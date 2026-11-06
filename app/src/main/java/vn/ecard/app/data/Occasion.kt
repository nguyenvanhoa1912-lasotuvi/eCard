package vn.ecard.app.data

import java.util.Locale

enum class Lang(val label: String, val locale: Locale) {
    VI("Tiếng Việt", Locale("vi", "VN")),
    EN("English", Locale.US),
    ZH("中文(繁體)", Locale.TRADITIONAL_CHINESE)
}

/** Văn bản 3 ngôn ngữ. Tiếng Trung dùng chữ Phồn thể. */
data class Tri(val vi: String, val en: String, val zh: String) {
    fun get(l: Lang): String = when (l) {
        Lang.VI -> vi
        Lang.EN -> en
        Lang.ZH -> zh
    }
}

enum class Category(val label: String) {
    CHUC_MUNG("Lời chúc"),
    CHIA_BUON("Lời chia buồn"),
    LOI_MOI("Lời mời"),
    LOI_HAY("Lời hay ý đẹp")
}

enum class Occasion(val title: Tri, val category: Category, val icon: String, val solemn: Boolean = false) {

    TET(Tri("Chúc Mừng Năm Mới", "Happy Lunar New Year", "恭賀新禧"), Category.CHUC_MUNG, "🧧"),
    NEW_YEAR(Tri("Chúc Mừng Năm Mới", "Happy New Year", "新年快樂"), Category.CHUC_MUNG, "🎆"),
    BIRTHDAY(Tri("Chúc Mừng Sinh Nhật", "Happy Birthday", "生日快樂"), Category.CHUC_MUNG, "🎂"),
    LONGEVITY(Tri("Mừng Thọ", "Happy Longevity", "福壽康寧"), Category.CHUC_MUNG, "🍑"),
    WEDDING(Tri("Chúc Mừng Hạnh Phúc", "Happy Wedding", "新婚快樂"), Category.CHUC_MUNG, "💍"),
    HOUSEWARMING(Tri("Mừng Tân Gia", "Happy Housewarming", "喬遷之喜"), Category.CHUC_MUNG, "🏡"),
    OPENING(Tri("Mừng Khai Trương", "Grand Opening", "開業大吉"), Category.CHUC_MUNG, "🎊"),
    GRADUATION(Tri("Chúc Mừng Tốt Nghiệp", "Congratulations, Graduate", "畢業快樂"), Category.CHUC_MUNG, "🎓"),
    PROMOTION(Tri("Chúc Mừng Thăng Chức", "Congratulations on Your Promotion", "恭喜高升"), Category.CHUC_MUNG, "📈"),
    BABY(Tri("Mừng Bé Chào Đời", "Welcome, Little One", "喜迎寶貝"), Category.CHUC_MUNG, "👶"),
    WOMEN_DAY(Tri("Mừng Ngày Quốc Tế Phụ Nữ", "Happy Women's Day", "婦女節快樂"), Category.CHUC_MUNG, "🌷"),
    VN_WOMEN_DAY(Tri("Mừng Ngày Phụ Nữ Việt Nam", "Happy Vietnamese Women's Day", "越南婦女節快樂"), Category.CHUC_MUNG, "💐"),
    TEACHER_DAY(Tri("Tri Ân Thầy Cô", "Happy Teachers' Day", "教師節快樂"), Category.CHUC_MUNG, "📚"),
    MID_AUTUMN(Tri("Vui Tết Trung Thu", "Happy Mid-Autumn Festival", "中秋節快樂"), Category.CHUC_MUNG, "🥮"),
    CHRISTMAS(Tri("Giáng Sinh An Lành", "Merry Christmas", "聖誕快樂"), Category.CHUC_MUNG, "🎄"),
    VALENTINE(Tri("Valentine Ngọt Ngào", "Happy Valentine's Day", "情人節快樂"), Category.CHUC_MUNG, "❤️"),
    THANKS(Tri("Xin Chân Thành Cảm Ơn", "Thank You", "衷心感謝"), Category.CHUC_MUNG, "🙏"),

    CONDOLENCE(Tri("Thành Kính Phân Ưu", "Deepest Condolences", "沉痛哀悼"), Category.CHIA_BUON, "🕯️", solemn = true),
    MEMORIAL(Tri("Tưởng Nhớ", "In Loving Memory", "永遠懷念"), Category.CHIA_BUON, "🤍", solemn = true),

    INVITE_WEDDING(Tri("Thiệp Mời Cưới", "Wedding Invitation", "婚禮請柬"), Category.LOI_MOI, "💌"),
    INVITE_PARTY(Tri("Trân Trọng Kính Mời", "You're Invited", "誠摯邀請"), Category.LOI_MOI, "🥂"),
    INVITE_MEMORIAL(Tri("Kính Mời Dự Lễ Giỗ", "Memorial Ceremony Invitation", "敬邀出席追思禮"), Category.LOI_MOI, "🪷", solemn = true),

    WISDOM(Tri("Lời Hay Ý Đẹp", "Words of Wisdom", "嘉言錦句"), Category.LOI_HAY, "✨");

    /** Nhãn hiển thị trên ô chọn dịp (ngắn, không trùng nhau). */
    val label: String get() = when (this) {
        TET -> "Tết Nguyên Đán"
        NEW_YEAR -> "Năm Mới Dương Lịch"
        else -> title.vi
    }

}
