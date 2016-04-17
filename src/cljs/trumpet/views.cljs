(ns trumpet.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn valve [trumpet-state id]
  (if (contains? @trumpet-state id)
    [:div.down-key
     [:div.across]]
    [:div.key
     [:div.across]
     [:div.upright]]))

(defn home-page []
  (let [key-press (subscribe [:key-press])
        playing (subscribe [:playing])
        trumpet-state (subscribe [:trumpet-state])
        buzz (subscribe [:buzz])
        value (reaction (reduce + (* 10 @buzz) @trumpet-state))
        buzzer (r/atom 1)]
    (fn []
      [:div
       [:h2 (str "Trumpet ")]
       [:p (str "kp: " @key-press)]
       [:p (str "ts: " @trumpet-state)]
       [:div.keys
        [:div.buzzer {:class (if @playing "buzzing" "silent")}
         [:div
          [:p "BUZZ"]] 
         [:input {:type :range
                  :orient "vertical"
                  :min 0
                  :max 5
                  :step 1
                  :defaultValue 1
                  :val @buzz
                  :on-input #(do (reset! buzzer (-> % .-target .-value))
                                 (dispatch [:buzz @buzzer]))
                  :on-mouse-up #(dispatch [:stop-sound])}]
         
         [:div
          [:p @buzzer]]]
        [valve trumpet-state 1]
        [valve trumpet-state 2]
        [valve trumpet-state 4]
        ]])))
