(ns panaeolus.broker
  (:require
   [cljs.core.async
    :refer [<! >! chan timeout]]
   [panaeolus.engine
    :refer [poll-channel csound Csound BEAT]]
   [goog.string :as gstring])
  (:require-macros [cljs.core.async.macros
                    :refer [go go-loop]]))

(defn event-shooter [env pat-name
                     index a-index]
  (let [{:keys [xtratim]} env]
    (loop [pn 3
           ps [(gstring/format "i \"%s\" 0" pat-name)]]
      (let [p (keyword (str "p" pn))]
        (if-not (contains? env p)
          (prn (apply str (interpose "\n" ps)))
          #_(csnd6/csoundInputMessage csound
                                      (apply str (interpose "\n" ps)))
          (recur
           (inc pn)
           (let [
                 link (get env p)
                 cur-p (if (vector? link)
                         (get-in env link)
                         (get env link))
                 cur-p (if (number? cur-p)
                         (conj [] cur-p)
                         cur-p)
                 func? (fn? cur-p)
                 cur-p (if func?
                         cur-p
                         (if (= :p3 p)
                           (if (:xtim env)
                             (cond
                               (fn? (:xtim env))
                               ((:xtim env) since-eval)
                               (number? (:xtim env)) (:xtim env)
                               :else
                               (nth (:xtim env) (mod a-index (count (:xtim env)))))
                             (* (if xtratim
                                  xtratim 1.0)
                                (nth cur-p
                                     (mod index (count cur-p)))))
                           (if (= :freq link)
                             (nth cur-p
                                  (mod index (count cur-p)))
                             (nth cur-p
                                  (mod a-index (count cur-p))))))]
             (cond
               func? (map #(str % " " (cur-p (int
                                              (/ (* since-eval
                                                    (/ (:mod-div env)
                                                       (let [dur (:dur env)]
                                                         (if (number? dur) 1
                                                             (count dur))))) 10)))) ps)
               (number? cur-p)
               (map #(str % " " (float cur-p)) ps)
               (vector? cur-p)
               (cond
                 (= (count cur-p)
                    (count ps))
                 (map #(str %1 " " (float %2)) ps cur-p)
                 (< (count cur-p)
                    (count ps))
                 (map #(str %1 " " (float %2))
                      ps (apply conj
                                cur-p
                                (repeat
                                 (- (count ps)
                                    (count cur-p))
                                 (last cur-p))))
                 (> (count cur-p)
                    (count ps))
                 (map #(str %1 " " (float %2))
                      (take (count cur-p) (cycle ps))
                      cur-p))))))))))

(defn event-shooter
  [env pat-name
   index a-index since-eval]
  (let [xtratim (if-let [xtr (:xtratim env)]
                  (cond
                    (fn? xtr)
                    (xtr since-eval)
                    (number? xtr) xtr
                    :else
                    (nth xtr (mod a-index (count xtr))))
                  nil)]
    (loop [pn 3
           ps [(gstring/format "i \"%s\" 0" pat-name)]]
      (let [p (keyword (str "p" pn))]
        (if-not (contains? env p)
          (prn (apply str (interpose "\n" ps)))
          #_(csnd6/csoundInputMessage csound
                                      (apply str (interpose "\n" ps)))
          (recur
           (inc pn)
           (let [
                 link (get env p)
                 cur-p (if (vector? link)
                         (get-in env link)
                         (get env link))
                 cur-p (if (number? cur-p)
                         (conj [] cur-p)
                         cur-p)
                 func? (fn? cur-p)
                 cur-p (if func?
                         cur-p
                         (if (= :p3 p)
                           (if (:xtim env)
                             (cond
                               (fn? (:xtim env))
                               ((:xtim env) since-eval)
                               (number? (:xtim env)) (:xtim env)
                               :else
                               (nth (:xtim env) (mod a-index (count (:xtim env)))))
                             (* (if xtratim
                                  xtratim 1.0)
                                (nth cur-p
                                     (mod index (count cur-p)))))
                           (if (= :freq link)
                             (nth cur-p
                                  (mod index (count cur-p)))
                             (nth cur-p
                                  (mod a-index (count cur-p))))))]
             (cond
               func? (map #(str % " " (cur-p (int
                                              (/ (* since-eval
                                                    (/ (:mod-div env)
                                                       (let [dur (:dur env)]
                                                         (if (number? dur) 1
                                                             (count dur))))) 10)))) ps)
               (number? cur-p)
               (map #(str % " " (float cur-p)) ps)
               (vector? cur-p)
               (cond
                 (= (count cur-p)
                    (count ps))
                 (map #(str %1 " " (float %2)) ps cur-p)
                 (< (count cur-p)
                    (count ps))
                 (map #(str %1 " " (float %2))
                      ps (apply conj
                                cur-p
                                (repeat
                                 (- (count ps)
                                    (count cur-p))
                                 (last cur-p))))
                 (> (count cur-p)
                    (count ps))
                 (map #(str %1 " " (float %2))
                      (take (count cur-p) (cycle ps))
                      cur-p))))))))))

(defn calc-mod-div [meter durations event-queue]
  (let [meter (if meter
                (if (< 0 meter) meter 0)
                0)]
    (if (< 0 meter)
      (let [bar-length (* BEAT meter)]
        (println bar-length (last event-queue))
        (* bar-length
           (quot bar-length
                 (last event-queue))))
      (* BEAT
         (if (number? durations)
           (js/Math.abs durations)
           (apply + (map #(js/Math.abs %) durations)))))))

(quot 440 1)

(calc-mod-div 4 [1 1 2 4 9000 9000] (dur->event-queue #queue [] [1 1]))

(defn dur->event-queue [durations event-queue]
  (into event-queue
        (if (number? durations)
          (list durations)
          (loop [dur (remove #(or (neg? %)
                                  (zero? %)) durations)
                 silence 0
                 last-dur 0
                 at []]
            (if (empty? dur)
              at
              (let [fdur (first dur)]
                (recur (rest dur)
                       (if (neg? fdur)
                         (+ silence (* BEAT (Math/abs fdur)))
                         0)
                       (if (neg? fdur)
                         last-dur
                         (* BEAT fdur))
                       (if (neg? fdur)
                         at
                         (conj at ((fn [v]
                                     (if (float? v)
                                       (Math/round v) v))
                                   (+ last-dur
                                      silence
                                      (if (empty? at)
                                        0 (last at)))))))))))))

(dur->event-queue [1 2 3 5] #queue [])

(defn pattern-loop-queue []
  (go-loop [index 0
            a-index 0
            env {:mod-div initial-mod-div
                 :mod-div-sync 0
                 :queue-buffer initial-queue}
            pending-env nil]))

(defn p* [env pat-name]
  (let [user-input-channel (chan (async/sliding-buffer 1))]
    ))


(go (let [event-c (chan)]
      (>! poll-channel [0 300 event-c])
      (when (<! event-c)
        (.InputMessage csound Csound "i 2 0 1"))))

(.CompileOrc csound Csound
             "instr 2\nasig poscil 0.9, 280\nouts asig,asig\nendin")

(.InputMessage csound Csound "i 2 0 2")


