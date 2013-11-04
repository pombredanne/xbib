package org.xbib.re;

public class UnicodeCategoryCharSet extends GenericCharSet {

    public static CharSet get(int type) {
        if (type <= 0 || type >= csets.length || type == 17) {
            throw new IllegalArgumentException("Unknown type " + type);
        }
        return csets[type];
    }

    private static final CharSet[] csets = {
            null,
            new UnicodeCategoryCharSet(1, 1190, 120744),
            new UnicodeCategoryCharSet(2, 1415, 120777),
            new CharSetBuilder().add(453).add(456).add(459).add(498).add(8072, 8079).add(8088, 8095).add(8104, 8111).add(8124).add(8140).add(8188).build(),
            new CharSetBuilder().add(688, 705).add(710, 721).add(736, 740).add(750).add(890).add(1369).add(1600).add(1765, 1766).add(3654).add(3782).add(6103).add(6211).add(7468, 7521).add(12293).add(12337, 12341).add(12347).add(12445, 12446).add(12540, 12542).add(65392).add(65438, 65439).build(),
            new UnicodeCategoryCharSet(5, 87797, 195101),
            new UnicodeCategoryCharSet(6, 792, 917999),
            new CharSetBuilder().add(1160, 1161).add(1758).add(8413, 8416).add(8418, 8420).build(),
            new UnicodeCategoryCharSet(8, 139, 119154),
            new CharSetBuilder().add(48, 57).add(1632, 1641).add(1776, 1785).add(2406, 2415).add(2534, 2543).add(2662, 2671).add(2790, 2799).add(2918, 2927).add(3047, 3055).add(3174, 3183).add(3302, 3311).add(3430, 3439).add(3664, 3673).add(3792, 3801).add(3872, 3881).add(4160, 4169).add(4969, 4977).add(6112, 6121).add(6160, 6169).add(6470, 6479).add(65296, 65305).add(66720, 66729).add(120782, 120831).build(),
            new CharSetBuilder().add(5870, 5872).add(8544, 8579).add(12295).add(12321, 12329).add(12344, 12346).add(66378).build(),
            new CharSetBuilder().add(178, 179).add(185).add(188, 190).add(2548, 2553).add(3056, 3058).add(3882, 3891).add(4978, 4988).add(6128, 6137).add(8304).add(8308, 8313).add(8320, 8329).add(8531, 8543).add(9312, 9371).add(9450, 9471).add(10102, 10131).add(12690, 12693).add(12832, 12841).add(12881, 12895).add(12928, 12937).add(12977, 12991).add(65799, 65843).add(66336, 66339).build(),
            new CharSetBuilder().add(32).add(160).add(5760).add(6158).add(8192, 8203).add(8239).add(8287).add(12288).build(),
            new CharSetBuilder().add(8232).build(),
            new CharSetBuilder().add(8233).build(),
            new CharSetBuilder().add(0, 31).add(127, 159).build(),
            new CharSetBuilder().add(173).add(1536, 1539).add(1757).add(1807).add(6068, 6069).add(8204, 8207).add(8234, 8238).add(8288, 8291).add(8298, 8303).add(65279).add(65529, 65531).add(119155, 119162).add(917505).add(917536, 917631).build(),
            null,
            new CharSetBuilder().add(57344, 63743).add(983040, 1048573).add(1048576, 1114109).build(),
            new CharSetBuilder().add(55296, 57343).build(),
            new CharSetBuilder().add(45).add(1418).add(6150).add(8208, 8213).add(12316).add(12336).add(12448).add(65073, 65074).add(65112).add(65123).add(65293).build(),
            new CharSetBuilder().add(40).add(91).add(123).add(3898).add(3900).add(5787).add(8218).add(8222).add(8261).add(8317).add(8333).add(9001).add(9140).add(10088).add(10090).add(10092).add(10094).add(10096).add(10098).add(10100).add(10214).add(10216).add(10218).add(10627).add(10629).add(10631).add(10633).add(10635).add(10637).add(10639).add(10641).add(10643).add(10645).add(10647).add(10712).add(10714).add(10748).add(12296).add(12298).add(12300).add(12302).add(12304).add(12308).add(12310).add(12312).add(12314).add(12317).add(64830).add(65077).add(65079).add(65081).add(65083).add(65085).add(65087).add(65089).add(65091).add(65095).add(65113).add(65115).add(65117).add(65288).add(65339).add(65371).add(65375).add(65378).build(),
            new CharSetBuilder().add(41).add(93).add(125).add(3899).add(3901).add(5788).add(8262).add(8318).add(8334).add(9002).add(9141).add(10089).add(10091).add(10093).add(10095).add(10097).add(10099).add(10101).add(10215).add(10217).add(10219).add(10628).add(10630).add(10632).add(10634).add(10636).add(10638).add(10640).add(10642).add(10644).add(10646).add(10648).add(10713).add(10715).add(10749).add(12297).add(12299).add(12301).add(12303).add(12305).add(12309).add(12311).add(12313).add(12315).add(12318, 12319).add(64831).add(65078).add(65080).add(65082).add(65084).add(65086).add(65088).add(65090).add(65092).add(65096).add(65114).add(65116).add(65118).add(65289).add(65341).add(65373).add(65376).add(65379).build(),
            new CharSetBuilder().add(95).add(8255, 8256).add(8276).add(12539).add(65075, 65076).add(65101, 65103).add(65343).add(65381).build(),
            new UnicodeCategoryCharSet(24, 202, 66463),
            new UnicodeCategoryCharSet(25, 899, 120771),
            new CharSetBuilder().add(36).add(162, 165).add(2546, 2547).add(2801).add(3065).add(3647).add(6107).add(8352, 8369).add(65020).add(65129).add(65284).add(65504, 65505).add(65509, 65510).build(),
            new CharSetBuilder().add(94).add(96).add(168).add(175).add(180).add(184).add(706, 709).add(722, 735).add(741, 749).add(751, 767).add(884, 885).add(900, 901).add(8125).add(8127, 8129).add(8141, 8143).add(8157, 8159).add(8173, 8175).add(8189, 8190).add(12443, 12444).add(65342).add(65344).add(65507).build(),
            new UnicodeCategoryCharSet(28, 2745, 119638),
            new CharSetBuilder().add(171).add(8216).add(8219, 8220).add(8223).add(8249).build(),
            new CharSetBuilder().add(187).add(8217).add(8221).add(8250).build(),};
    private final int type;
    private final int cardinality;

    private UnicodeCategoryCharSet(int type, int cardinality, int last) {
        super(last);
        this.type = type;
        this.cardinality = cardinality;
    }

    @Override
    public boolean contains(int c) {
        return Character.getType(c) == type;
    }

    @Override
    public int cardinality() {
        return cardinality;
    }
}
