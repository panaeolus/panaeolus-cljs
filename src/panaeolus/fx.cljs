(ns panaeolus.fx
  (:import [goog.string format]))


(defn freeverb
  [& {:keys [room damp sr]
      :or {room 0.9 damp 0.35 sr 44100}}]
  (fn [aL aR]
    (str (format"\n%s, %s freeverb %s*1.5, %s*1.5, %f, %f, %d\n" aL aR aL aR room damp sr)
         "kRvbEnv expsegr 1, p3, 1, p3*2, .01\n"
         aL " *= kRvbEnv\n" aR " *= kRvbEnv\n")))

(defn lofi [& {:keys [bits fold]
               :or {bits 6 fold 0.1}}]
  (fn [aL aR]
    (format"\n%s, %s LoFiS  %s, %s, %f, %f\n" aL aR aL aR bits fold)))


(defn flanger [& {:keys [rate depth delay fback wet shape]
                  :or {rate 5.15 depth 0.001
                       delay 0.001 fback 0
                       wet 1 shape 1}}]
  (fn [aL aR]
    (format "\n %s, %s Flanger_stereo %s, %s, %s, %s, %s, %s, %s, %s \n"
            aL aR aL aR rate depth delay fback wet shape)))

(defn delayl [& {:keys [base shape ival scatter spread
                        mode hpf lpf fback layers]
                 :or {base 50 layers 8 ival 0.07
                      shape 1 scatter 0 spread 0.5
                      fback 0.95 hpf 70 lpf 70 mode 1}}]
  (fn [aL aR]
    (format  "\n%s, %s DelayLayer %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n"
             aL aR aL aR base shape ival scatter spread mode hpf lpf fback layers)))

(defn perc [& {:keys [dur exp]
               :or {dur 1 exp 0.1}}]
  (fn [aL aR]
    (format "\np3=%f\naPercEnv expon 1,%f,%f\n%s*=aPercEnv\n%s*=aPercEnv\n"
            (* 1.1 dur) dur exp aL aR)))


(comment
  (freeverb :room 1)

  ((freeverb :sr 22.2) "aRight" "aLeft")

  (gstring/format "%d" 1))
