package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectRswk extends MABElement {

    private final static MABElement element = new SubjectRswk();

    private SubjectRswk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    /*
         * alt: 9 ID p Personenschlagwort g geographisch-ethnographisches
         * Schlagwort s Sachschlagwort k Koerperschaftsschlagwort: Ansetzung
         * unter dem Individualnamen c Koerperschaftsschlagwort: Ansetzung unter
         * dem Ortssitz z Zeitschlagwort f Formschlagwort t Werktitel als
         * Schlagwort blank Unterschlagwort einer Ansetzungskette
         *
         *
         * neu: 902: Unterfelder: p = Personenschlagwort (NW) g = Geografikum
         * (Gebietskörperschaft) (NW) e = Kongressname (NW) k = Körperschaft s =
         * Sachschlagwort (NW), Version (NW) b = Untergeordnete Körperschaft,
         * untergeordnete Einheit (W) c = Beiname (NW), Ort (NW) d = Datum (NW)
         * h = Zusatz (W) z = Zeitschlagwort = geographische Unterteilung (W) f
         * = Formschlagwort (NW), Erscheinungsjahr eines Werkes (NW) t =
         * Werktitel als Schlagwort (NW) m = Besetzung im Musikbereich (W) n =
         * Zählung (NW) o = Angabe des Musikarrangements (NW) u = Titel eines
         * Teils/einer Abteilung eines Werkes (W) r = Tonart (NW) x =
         * nachgeordneter Teil (W) 9 = GND-Identifikationsnummer a =
         * (Alt-)Schlagwort ohne IDN-Verknüpfung (NW)
         *
     */

}
