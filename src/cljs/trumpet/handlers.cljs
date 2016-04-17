(ns trumpet.handlers
  (:require [re-frame.core :refer [register-handler debug dispatch]]
            [hum.core :as hum]))

(def ctx (hum/create-context))
(def vco (hum/create-osc ctx :square))
(def vcf (hum/create-biquad-filter ctx))
(def output (hum/create-gain ctx))
(def twelfth-root-of-two (.pow js/Math 2 (/ 1 12)))
(def a-number 49)
(def a-frequency 440)
(def note-numbers
  {32 55, 1 38, 33 53, 2 39, 3 37, 40 56, 41 57, 10 40, 42 58, 11 45,
   12 46, 13 44, 15 42, 16 43, 17 41, 50 59, 51 62, 20 47, 52 63, 21 50,
   53 61, 22 51, 23 49, 56 60, 26 48, 60 64, 30 52, 31 54} )



(hum/connect vco vcf)
(hum/connect vcf output)
(hum/start-osc vco)
(hum/connect-output output)


(defn freq [target]
  (* a-frequency (.pow js/Math twelfth-root-of-two (- target a-number))))

(defn keydown [e]
  (dispatch [:key-down e]))

(defn keyup [e]
  (dispatch [:key-up e]))

(defn state-val [e]
  (case (.-keyCode e)
    83 1
    68 2
    70 4
    74 1
    75 2
    76 4
    nil))

(def state {:valve-keys #{83 68 70 74 75 76} ;; s d f j k l
            :buzz 1
            :trumpet-state #{}})

(register-handler
 :initialize
 debug
 (fn [db _]
   (set! (.-onkeydown js/document) keydown)
   (set! (.-onkeyup js/document) keyup)
   (merge db state)))

(register-handler
 :buzz
 debug
 (fn [db [_ buzz]]
   (let [new-buzz? (not= (:buzz db) buzz)
         db (assoc db :buzz buzz)]
     (when new-buzz? (dispatch [:start-sound]))
     db)))

(register-handler
 :key-down
 debug
 (fn [db [_ e]]
   (let [db (if (contains? (:valve-keys db) (.-keyCode e))
              (update db :trumpet-state #(conj % (state-val e)))
              db)]
     (when (:playing db) (dispatch [:start-sound]))
     (assoc db :key-press (.-keyCode e))) ))

(register-handler
 :key-up
 debug
 (fn [db [_ e]]
   (update db :trumpet-state #(disj % (state-val e)))))

(register-handler
 :start-sound
 debug
 (fn [db _]
   (let [keys (+ (reduce + (:trumpet-state db)) (* 10 (:buzz db)))
         frequency (freq (get note-numbers keys))]
     (hum/note-on output vco frequency)
     (assoc db :playing true))))

(register-handler
 :stop-sound
 debug
 (fn [db [_ note]]
   (hum/note-off output)
   (assoc db :playing false)))


