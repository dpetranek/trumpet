(ns trumpet.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub
 :instrument
 (fn [db _]
   (reaction (:instrument @db))))

(register-sub
 :playing
 (fn [db _]
   (reaction (:playing @db))))

(register-sub
 :key-press
 (fn [db _]
   (reaction (:key-press @db))))

(register-sub
 :trumpet-state
 (fn [db _]
   (reaction (:trumpet-state @db))))

(register-sub
 :buzz
 (fn [db _]
   (reaction (:buzz @db))))
