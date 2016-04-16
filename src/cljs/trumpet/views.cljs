(ns trumpet.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn valve [key-press id]
  (if (contains? @key-press id)
    [:div.down-key
     [:div.across]]
    [:div.key
     [:div.across]
     [:div.upright]]))

(defn home-page []
  (let [instrument (subscribe [:instrument])
        playing (subscribe [:playing])
        key-press (subscribe [:key-press])
        buzz (subscribe [:buzz])
        value (reaction (reduce + (* 10 @buzz) @key-press))
        buzzer (r/atom 0)]
    (fn []
      [:div
       [:h2 (str "Trumpet ")  @value]
       [:p (str "key-press " @key-press)]
       [:p (str "buzz: " @buzz)]
       [:div.keys
        [valve key-press 1]
        [valve key-press 2]
        [valve key-press 4]
        [:div.buzzer {:class (if @playing "buzzing" "silent")}
         [:div
          [:p "BUZZ"]] 
         [:input {:type :range
                  :orient "vertical"
                  :min 0
                  :max 5
                  :step 1
                  :defaultValue 0
                  :val @buzz
                  :on-input #(do (reset! buzzer (-> % .-target .-value))
                                 (dispatch [:buzz @buzzer]))
                  :on-mouse-up #(dispatch [:stop-sound])}]
;; check to see if value has actually changed
;; change event is only triggered on mouse up
;; my code is doing extra triggering
         [:div
          [:p @buzzer]]]]])))
