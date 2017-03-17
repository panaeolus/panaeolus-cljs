(ns panaeolus.freq)

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

