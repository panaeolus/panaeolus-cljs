(ns panaeolus.instruments.fof
  (:require panaeolus.macros)
  (:require-macros [panaeolus.macros
                    :refer [definstrument demo ;; forever
                            ]
                    ]))


(definstrument "priest"
  "src/panaeolus/csound/orchestra/fof/priest.orc"
  {:p3 {:dur 1}
   :p4 {:amp -12}
   :p5 {:freq 200}
   :p6 {:morph 1}
   :p7 {:att 0.1}
   :p8 {:format 70}})

(def test {:p3 {:dur 1}
           :p4 {:amp -12}
           :p6 {:morph 1}
           :p5 {:freq 200}
           :p7 {:att 0.1}
           :p8 {:format 70}})

((first ((priest :freq 220 :amp 10) :aaa)))

#_(->> (-> test
           (dissoc :p3))
       (into (sorted-map))
       vals
       (map seq)
       (map first)
       (reduce #(into %1 %2) []))

#_(vals (into (sorted-map) (dissoc  :p3)))
;; (demo (priest :freq 200 :amp 10) 10)
