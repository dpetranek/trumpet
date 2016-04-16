(ns trumpet.handlers
  (:require [re-frame.core :refer [register-handler debug dispatch]]
            [hum.core :as hum]))

(def ctx (hum/create-context))
(def vco (hum/create-osc ctx :square))
(def vcf (hum/create-biquad-filter ctx))
(def output (hum/create-gain ctx))
#_(def twelfth-root-of-two (Math/pow 2 1/12))
(def a-number 49)
(def a-frequency 440)

(hum/connect vco vcf)
(hum/connect vcf output)
(hum/start-osc vco)
(hum/connect-output output)

#_(defn freq [target]
  (* a-frequency (Math/pow twelfth-root-of-two (- target a-number))))

(defn keydown [e]
  (when (contains? #{83 68 70} (.-keyCode e)) (.preventDefault e)) ;; s d f
  (dispatch [:key-down e]))

(defn keyup [e]
  (dispatch [:key-up e]))



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
 (fn [db [_ e]]
   (update db :key-press #(conj % (.-keyCode e)))))

(register-handler
 :key-up
 debug
 (fn [db [_ e]]
   (update db :key-press #(disj % (.-keyCode e)))))

(register-handler
 :start-sound
 debug
 (fn [db _]
   (hum/note-on output vco 440)
   (assoc db :playing true)))

(register-handler
 :stop-sound
 debug
 (fn [db [_ note]]
   (hum/note-off output)
   (assoc db :playing false)))


