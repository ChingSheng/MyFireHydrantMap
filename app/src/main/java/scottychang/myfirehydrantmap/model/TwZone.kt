package scottychang.cafe_walker.model

enum class TwZone(var type: String) {
    UNKNOWN(""),
    SHANG_CHUN("shang_chun"),
    CHUNG_HO("chung_ho"),
    CHUNG_SHAN("chung_shan"),
    CHUNG_ZHENG("chung_zheng"),
    SHIN_YI("shin_yi"),
    NAY_HU("nay_hu"),
    BEI_TOU("bei_tou"),
    NAN_GANG("nan_gang"),
    SHIH_LIN("shih_lin"),
    DA_TONG("da_tong"),
    DA_AN("da_an"),
    WEN_SHAN("wen_shan"),
    SHIN_DIEN("shin_dien"),
    SUNG_SHAN("sung_shan"),
    YONG_HO("yong_ho"),
    SHI_ZI("shi_zi"),
    WAN_HUA("wan_hua");

    override fun toString(): String {
        return type // working!
    }
}