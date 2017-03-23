(ns panaeolus.freq
  (:require [clojure.string :as string]))

(defn midi->freq
  "Convert MIDI Note number to frequency in hertz
   from Steven Yi's score"
  [notenum]
  (* 440 (Math/pow 2.0 (/ (- notenum 69) 12))))

(defn freq->midi
  "Truncates freq to nearest midi val."
  [freq]
  (Math/round
   (+ 69 (* 12 (/ (Math/log (/ freq 440))
                  (Math/log 2))))))


(def note->midi {:C  0  :c  0  :b# 0  :B# 0  :his 0  :His 0
                 :C# 1  :c# 1  :Db 1  :db 1  :DB 1  :dB 1 :cis 1 :Cis 1
                 :D  2  :d  2
                 :D# 3  :d# 3  :Eb 3  :eb 3  :EB 3  :eB 3 :es 3 :Es 3
                 :E  4  :e  4  :Fb 4  :fb 4  :Fes 4 :fes 4
                 :E# 5  :e# 5  :F  5  :f  5  :eis 5 :Eis 5
                 :F# 6  :f# 6  :Gb 6  :gb 6  :GB 6  :gB 6 :fis 6 :Fis 6 :ges 6 :Ges 6
                 :G  7  :g  7
                 :G# 8  :g# 8  :Ab 8  :ab 8  :AB 8  :aB 8 :gis 8 :Gis 8 :as 8 :As 8
                 :A  9  :a  9
                 :A# 10 :a# 10 :Bb 10 :bb 10 :BB 10 :bB 10 :ais 10 :Ais 10 :hes 10 :Hes 10
                 :B  11 :b  11 :Cb 11 :cb 11 :CB 11 :cB 11 :H 11   :h 11   :Ces 11   :ces 11
                                        ;NOTE->OCTAVE NUMBER
                 :c0 12
                 :cis0 13 :des0 13
                 :d0 14 :dis0 15 :es0 15 :e0 16 :fes0 16 :f0 17 :eis0 17 :fis0 18 :ges0 18 :g0 19 :gis0 20 :as0 20
                 :a0 21 :b0 22 :ais0 22 :h0 23 :ces1 23
                 :c1 24
                 :cis1 25 :des1 25 :d1 26 :dis1 27 :es1 27 :e1 28 :fes1 28 :f1 29 :eis1 29
                 :fis1 30 :ges1 30 :g1 31 :gis1 32 :as1 32 :a1 33 :ais1 34 :b1 34 :h1 35 :ces2 35
                 :c2 36
                 :cis2 37 :des2 37 :d2 38 :dis2 39 :es2 39 :e2 40 :fes2 40 :f2 41 :eis2 41
                 :fis2 42 :ges2 42 :g2 43 :gis2 44 :as2 44 :a2 45 :ais2 46 :b2 46 :h2 47 :ces3 47
                 :c3 48
                 :cis3 49 :des3 49 :d3 50 :dis3 51 :es3 51 :e3 52 :fes3 52 :f3 53 :eis3 53
                 :fis3 54 :ges3 54 :g3 55 :gis3 56 :as3 56 :a3 57 :ais3 58 :b3 58 :h3 59 :ces4 59
                 :c4 60
                 :cis4 61 :des4 61 :d4 62 :dis4 63 :es4 63 :e4 64 :fes4 64 :f4 65 :eis4 65
                 :fis4 66 :ges4 66 :g4 67 :gis4 68 :as4 68 :a4 69 :ais4 70 :b4 70 :h4 71 :ces5 71
                 :c5 72
                 :cis5 73 :des5 73 :d5 74 :dis5 75 :es5 75 :e5 76 :fes5 76 :f5 77 :eis5 77
                 :fis5 78 :ges5 78 :g5 79 :gis5 80 :as5 80 :a5 81 :ais5 82 :b5 82 :h5 83 :ces6 83
                 :c6 84
                 :cis6 85 :des6 85 :d6 86 :dis6 87 :es6 87 :e6 88 :fes6 88 :f6 89 :eis6 89
                 :fis6 90 :ges6 90 :g6 91 :gis6 92 :as6 92 :a6 93 :ais6 94 :b6 94 :h6 95 :ces7 95
                 :c7 96
                 :cis7 97 :des7 97 :d7 98 :dis7 99 :es7 99 :e7 100 :fes7 100 :f7 101 :eis7 101
                 :fis7 102 :ges7 102 :g7 103 :gis7 104 :as7 104 :a7 105 :ais7 106 :b7 106 :h7 107 :ces8 107
                 :c8 108
                 :cis8 109 :des8 109 :d8 110 :dis8 111 :es8 111 :e8 112 :fes8 112 :f8 113 :eis8 114
                 :fis8 115 :ges8 115 :g8 116 :gis8 117 :as8 117 :a8 118 :ais8 119 :b8 119 :h8 120 :ces9 120
                 :c9 121
                 :cis9 122 :des9 122 :d9 123 :dis9 124 :es9 124 :e9 125 :fes9 125 :f9 126 :eis9 126
                 :fis9 127 :ges9 127 :g9 128 :gis9 129 :as9 129 :a9 130 :ais9 131 :b9 131 :h9 132 :ces10 132})

(def modes
  "from overtone"
  (let [ionian-sequence     [2 2 1 2 2 2 1]
        hex-sequence        [2 2 1 2 2 3]
        pentatonic-sequence [3 2 2 3 2]
        rotate (fn [scale-sequence offset]
                 (take (count scale-sequence)
                       (drop offset (cycle scale-sequence))))]
    {:diatonic           ionian-sequence
     :dúr                ionian-sequence
     :ionian             (rotate ionian-sequence 0)
     :major              (rotate ionian-sequence 0)
     :dorian             (rotate ionian-sequence 1)
     :dórískur           (rotate ionian-sequence 1)
     :phrygian           (rotate ionian-sequence 2)
     :frýgískur          (rotate ionian-sequence 2)
     :lydian             (rotate ionian-sequence 3)
     :lýdískur           (rotate ionian-sequence 3)
     :mixolydian         (rotate ionian-sequence 4)
     :mixólýdískur       (rotate ionian-sequence 4)
     :aeolian            (rotate ionian-sequence 5)
     :eólískur           (rotate ionian-sequence 5)
     :minor              (rotate ionian-sequence 5)
     :moll               (rotate ionian-sequence 5)
     :locrian            (rotate ionian-sequence 6)
     :hex-major6         (rotate hex-sequence 0)
     :hex-dorian         (rotate hex-sequence 1)
     :hex-phrygian       (rotate hex-sequence 2)
     :hex-major7         (rotate hex-sequence 3)
     :hex-sus            (rotate hex-sequence 4)
     :hex-aeolian        (rotate hex-sequence 5)
     :minor-pentatonic   (rotate pentatonic-sequence 0)
     :yu                 (rotate pentatonic-sequence 0)
     :major-pentatonic   (rotate pentatonic-sequence 1)
     :gong               (rotate pentatonic-sequence 1)
     :egyptian           (rotate pentatonic-sequence 2)
     :shang              (rotate pentatonic-sequence 2)
     :jiao               (rotate pentatonic-sequence 3)
     :pentatonic         (rotate pentatonic-sequence 4) ;; historical match
     :zhi                (rotate pentatonic-sequence 4)
     :ritusen            (rotate pentatonic-sequence 4)
     :whole-tone         [2 2 2 2 2 2]
     :whole              [2 2 2 2 2 2]
     :heiltóna           [2 2 2 2 2 2]
     :chromatic          [1 1 1 1 1 1 1 1 1 1 1 1]
     :harmonic-minor     [2 1 2 2 1 3 1]
     :hlj-moll           [2 1 2 2 1 3 1]
     :melodic-minor-asc  [2 1 2 2 2 2 1]
     :hungarian-minor    [2 1 3 1 1 3 1]
     :octatonic          [2 1 2 1 2 1 2 1]
     :messiaen1          [2 2 2 2 2 2]
     :messiaen2          [1 2 1 2 1 2 1 2]
     :messiaen3          [2 1 1 2 1 1 2 1 1]
     :messiaen4          [1 1 3 1 1 1 3 1]
     :messiaen5          [1 4 1 1 4 1]
     :messiaen6          [2 2 1 1 2 2 1 1]
     :messiaen7          [1 1 1 2 1 1 1 1 2 1]
     :super-locrian      [1 2 1 2 2 2 2]
     :hirajoshi          [2 1 4 1 4]
     :kumoi              [2 1 4 2 3]
     :neapolitan-major   [1 2 2 2 2 2 1]
     :bartok             [2 2 1 2 1 2 2]
     :bhairav            [1 3 1 2 1 3 1]
     :locrian-major      [2 2 1 1 2 2 2]
     :ahirbhairav        [1 3 1 2 2 1 2]
     :enigmatic          [1 3 2 2 2 1 1]
     :neapolitan-minor   [1 2 2 2 1 3 1]
     :pelog              [1 2 4 1 4]
     :augmented2         [1 3 1 3 1 3]
     :scriabin           [1 3 3 2 3]
     :harmonic-major     [2 2 1 2 1 3 1]
     :melodic-minor-desc [2 1 2 2 1 2 2]
     :romanian-minor     [2 1 3 1 2 1 2]
     :hindu              [2 2 1 2 1 2 2]
     :iwato              [1 4 1 4 2]
     :melodic-minor      [2 1 2 2 2 2 1]
     :diminished2        [2 1 2 1 2 1 2 1]
     :marva              [1 3 2 1 2 2 1]
     :melodic-major      [2 2 1 2 1 2 2]
     :indian             [4 1 2 3 2]
     :spanish            [1 3 1 2 1 2 2]
     :prometheus         [2 2 2 5 1]
     :diminished         [1 2 1 2 1 2 1 2]
     :minnkaður          [1 2 1 2 1 2 1 2]
     :todi               [1 2 3 1 1 3 1]
     :leading-whole      [2 2 2 2 2 1 1]
     :augmented          [3 1 3 1 3 1]
     :purvi              [1 3 2 1 1 3 1]
     :chinese            [4 2 1 4 1]
     :lydian-minor       [2 2 2 1 1 2 2]}))




(defn scale-all [base mode]
  (reductions + (base note->midi) (take 80 (cycle (mode modes)))))

(defn index-of [e coll] (first (keep-indexed #(if (= e %2) %1) coll)))

(defn scale-rotate [scale-sequence offset]
  (take (count scale-sequence)
        (drop offset (cycle scale-sequence))))

(defn scale-from-midi
  "return a scale in vector"
  [root mode & {:keys [span]
                :or {span 8}}]
  (let [root (if (keyword? root)
               (root note->midi)
               root)]
    (vec (take (inc span) (map #(+ root  %) (reductions + 0 (cycle (mode modes))))))))

(comment 

  (defn scale
    "return a scale in vector"
    [root mode & {:keys [span]
                  :or {span 8}}]
    (vec (take span (doall (map #(+ (note->midi root) %) (reductions + 0 (mode modes)))))))


  (defn sæti [root key p & oct]
    (let [str-fn  (map name p)
          str-cnt-dot (map #(* -12 %)  (for [x str-fn] (count (filter #(= \. %) (seq x)))))
          str-cnt-comma (map #(* 12 %) (for [x str-fn] (count (filter #(= \' %) (seq x)))))
          flat-sharp-lookup (vec (for [x (range (count str-fn))] (if (= (first (nth str-fn x)) \#) 1
                                                                     (if (= (first (nth str-fn x)) \b) -1 0))))
          str-comma-dot (doall (mapv #(+ % %2) str-cnt-dot str-cnt-comma))
          str-del-comma (for [x str-fn] (apply str (remove #{\. \'} x)))
          str-del-b-s   (map #(string/replace % #"^b|^#" "") str-del-comma)
          grunnsæti {:i 0 :ii 1 :iii 2 :iv 3 :v 4 :vi 5 :vii 6
                     :I 0 :II 1 :III 2 :IV 3 :V 4 :VI 5 :VII 6}
          sæti-keyword (map keyword str-del-b-s)
          sæti-lookup  (map #(% grunnsæti) sæti-keyword)
          skali        (scale root key)
          skali-lookup (map #(get skali %) sæti-lookup)
          skali-lookup (if (nil? oct) skali-lookup (map #(+ (* 12 (+ (first oct))) %) skali-lookup))
          krom-oct-map (mapv #(+ % %2 %3) flat-sharp-lookup str-comma-dot skali-lookup)
          utkoma-hz    (mapv #(midi->freq %) krom-oct-map)]
      utkoma-hz))


  (defn scale-tool [root key p]
    (let [p (vector p)
          str-fn  (map name p)
          str-cnt-dot (map #(* -12 %)  (for [x str-fn] (count (filter #(= \. %) (seq x)))))
          str-cnt-comma (map #(* 12 %) (for [x str-fn] (count (filter #(= \' %) (seq x)))))
          flat-sharp-lookup (vec (for [x (range (count str-fn))] (if (= (first (nth str-fn x)) \#) 1
                                                                     (if (= (first (nth str-fn x)) \b) -1 0))))
          str-comma-dot (doall (mapv #(+ % %2) str-cnt-dot str-cnt-comma))
          str-del-comma (for [x str-fn] (apply str (remove #{\. \'} x)))
          str-del-b-s   (map #(string/replace % #"^b|^#" "") str-del-comma)
          grunnsæti {:i 0 :ii 1 :iii 2 :iv 3 :v 4 :vi 5 :vii 6
                     :I 0 :II 1 :III 2 :IV 3 :V 4 :VI 5 :VII 6}
          sæti-keyword (map keyword str-del-b-s)
          sæti-lookup  (map #(% grunnsæti) sæti-keyword)
          skali        (scale root key)
          skali-lookup (map #(get skali %) sæti-lookup)
          krom-oct-map (mapv #(+ % %2 %3) flat-sharp-lookup str-comma-dot skali-lookup)
          utkoma-hz    (mapv #(midi->freq %) krom-oct-map)]
      {:freq [utkoma-hz] :midi [krom-oct-map]})))


;; (scale-tool :c :major "i ii iv v")
