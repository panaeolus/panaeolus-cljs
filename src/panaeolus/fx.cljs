(ns panaeolus.fx
  (:import [goog.string format]))

(defn param-key [param-num]
  (keyword (str "p" param-num)))

(defn freeverb
  [& {:keys [room damp sr]
      :or {room 0.9 damp 0.35 sr 44100}}]
  (fn [aL aR]
    (str (format"\n%s, %s freeverb %s*1.5, %s*1.5, %f, %f, %d\n" aL aR aL aR room damp sr)
         "kRvbEnv expsegr 1, p3, 1, p3*2, .01\n"
         aL " *= kRvbEnv\n" aR " *= kRvbEnv\n")))

(defn lofi [& {:keys [bits fold] :as env}]
  (let [defaults {:bits 6 :fold 0.1}
        env (merge defaults env)]
    ;; (prn env)
    (fn [aL aR param-cnt]
      (let [param-1 (inc param-cnt)
            param-2 (inc param-1)]
        [(format"\n%s, %s LoFiS  %s, %s, p%s, p%s\n" aL aR aL aR param-1 param-2)
         {(param-key param-1) [:lofi :bits]
          (param-key param-2) [:lofi :fold]
          :lofi env
          :param-cnt param-2}]))))
;; (lofi :bits 20)

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
               :or {dur 0.4 exp 0.1}}]
  (fn [aL aR]
    (format "\np3=%f\naPercEnv expon 1,%f,%f\n%s*=aPercEnv\n%s*=aPercEnv\n"
            (* 1.1 dur) dur exp aL aR)))


(defn butbp [& {:keys [center band]
                :or {center 1000 band 100}}]
  (fn [aL aR]
    (format "\n%s butbp %s, %f,%f\n\n%s butbp %s, %f,%f\n"
            aL aL center band aR aR center band)))

(defn buthp [& cutoff]
  (let [cutoff (if (empty? cutoff)
                 200 (first cutoff))]
    (fn [aL aR]
      (format "\n%s buthp %s,%f\n\n%s buthp %s, %f\n"
              aL aL cutoff aR aR cutoff))))

(defn butlp [& cutoff]
  (let [cutoff (if (empty? cutoff)
                 1000 (first cutoff))]
    (fn [aL aR]
      (format "\n%s butlp %s,%f\n\n%s butlp %s, %f\n"
              aL aL cutoff aR aR cutoff))))

(defn vibrato [& freq]
  (let [delay1 0.1
        delay2 1.2
        freq (if (empty? freq)
               5.5 (first freq))]
    (fn [aL aR]
      (format "\n%s,%s Vibrato %s,%s,%f,%f,%f,giSine\n"
              aL aR aL aR freq delay1 delay2 ))))

(defn distort [& dist]
  (let [dist (if (empty? dist)
               0.5 (first dist))
        dist (max 0.05 dist)]
    (fn [aL aR]
      (format (str "\n%s distort %s*exp(%f), %f,giDrone \n"
                   "\n%s distort %s*exp(%f), %f,giDrone \n")
              aL aL dist dist aR aR dist dist))))

(comment
  (freeverb :room 1)

  ((freeverb :sr 22.2) "aRight" "aLeft")

  (gstring/format "%d" 1))
