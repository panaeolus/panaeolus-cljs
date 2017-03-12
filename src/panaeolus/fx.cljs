(ns panaeolus.fx
  (:require [goog.string :as gstring]))


(defn freeverb
  [& {:keys [room damp sr]
      :or {room 0.9 damp 0.35 sr 44100}}]
  (fn [aL aR]
    (gstring/format"\n%s, %s freeverb %s, %s, %f, %f, %d\n" aL aR aL aR room damp sr)))



(comment
  (freeverb :room 1)

  ((freeverb :sr 22.2) "aRight" "aLeft")

  (gstring/format "%d" 1))
