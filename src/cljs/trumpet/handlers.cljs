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
(def note-numbers [40 45 46 44 nil 42 43 41])

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
            :key-press #{}})

(register-handler
 :initialize
 debug
 (fn [db _]
   (set! (.-onkeydown js/document) keydown)
   (set! (.-onkeyup js/document) keyup)
   
   (merge db state)))

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
   (let [keys (reduce + (:key-press db))
         frequency (freq (get note-numbers keys))]
     (hum/note-on output vco frequency)
     (assoc db :playing true))))

(register-handler
 :stop-sound
 debug
 (fn [db [_ note]]
   (hum/note-off output)
   (assoc db :playing false)))


