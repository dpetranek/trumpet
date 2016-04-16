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
  {0 40, 32 55, 1 38, 33 53, 2 39, 3 37, 5 42, 6 43, 7 41, 40 59, 41 57,
   10 47, 42 58, 11 45, 43 61, 12 46, 13 44, 46 60, 16 48, 50 64, 51 62,
   20 52, 52 63, 21 50, 22 51, 23 49, 30 56, 31 54})

(hum/connect vco vcf)
(hum/connect vcf output)
(hum/start-osc vco)
(hum/connect-output output)


(defn freq [target]
  (* a-frequency (.pow js/Math twelfth-root-of-two (- target a-number))))

(defn keydown [e]
  (when (contains? #{83 68 70} (.-keyCode e)) (.preventDefault e)) ;; s d f
  (dispatch [:key-down e]))

(defn keyup [e]
  (dispatch [:key-up e]))

(defn keyval [e]
  (case (.-keyCode e)
    83 1
    68 2
    70 4
    nil))

(def state {:instrument "trumpet"
            :buzz 0
            :key-press #{}})

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
   (let [db (if (contains? #{83 68 70} (.-keyCode e))
              (update db :key-press #(conj % (keyval e)))
              db)]
     (when (:playing db) (dispatch [:start-sound]))
     db) ))

(register-handler
 :key-up
 debug
 (fn [db [_ e]]
   (update db :key-press #(disj % (keyval e)))))

(register-handler
 :start-sound
 debug
 (fn [db _]
   (let [keys (+ (reduce + (:key-press db)) (* 10 (:buzz db)))
         frequency (freq (get note-numbers keys))]
     (hum/note-on output vco frequency)
     (assoc db :playing true))))

(register-handler
 :stop-sound
 debug
 (fn [db [_ note]]
   (hum/note-off output)
   (assoc db :playing false)))


