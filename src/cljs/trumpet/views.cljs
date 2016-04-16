(ns trumpet.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn home-page []
  (let [instrument (subscribe [:instrument])
        playing (subscribe [:playing])
        key-press (subscribe [:key-press])]
    (fn []
      [:div
       [:h2 @instrument]
       [:p (str @key-press)]
       [:div.keys
        [:div.key (when (contains? @key-press 83) {:class "white"})]
        [:div.key (when (contains? @key-press 68) {:class "white"})]
        [:div.key (when (contains? @key-press 70) {:class "white"})]
        [:div.key
         {:on-mouse-down #(dispatch [:start-sound])
          :on-mouse-up #(dispatch [:stop-sound])
          :class (if @playing "blue" "red")}
         [:p "trumpet sound"]]]])))
