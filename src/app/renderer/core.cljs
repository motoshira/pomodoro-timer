(ns app.renderer.core
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [reagent.dom :as rd]
            [goog.string :as gstr]
            [helins.timer :as timer]))

(enable-console-print!)

;;;
;;; db
;;;

(defonce ^:private db
  (r/atom {:timer nil
           :init-time (* 60 30)
           :time (* 60 30)}))

;;;
;;; events
;;;

(defn- dec-time! []
  (swap! db update :time #(max 0 (- % 1))))

(defn- set-interval! []
  (js/setInterval
   dec-time!
   1000))

(defn- clear-interval! [this]
  (js/clearInterval this))

(defn- toggle-timer! []
  ;; TODO: 間接的にstart/stopしたい
  (if-not (get @db :timer)
    (do
      (swap! db assoc :timer (set-interval!)))
    (do
      (clear-interval! (:timer @db))
      (swap! db assoc :timer nil))))

(defn- reset-timer! []
  (when-let [timer (get @db :timer)]
    (clear-interval! timer))
  (reset! db
          (-> @db
              (assoc :timer nil)
              (assoc :time (:init-time @db)))))


;;;
;;; subs
;;;

(defn- sec->min-sec [seconds]
  {:minutes (quot seconds 60)
   :seconds (rem seconds 60)})


;;;
;;; views
;;;

(defn- make-styles []
  {:root-div (assoc {:height "420px"
                     :width "300px"
                     :display :flex
                     :flex-direction :column
                     :justify-content :center
                     :align-items :center
                     :background "#000"
                     :color "#FFF"}
                    :opacity (if (or (zero? (get @db :time))
                                     (nil? (get @db :timer)))
                               1
                               0.4))
   :message {:font-size "20px"
             :display :flex
             :flex-glow 1
             :margin-top "24px"}
   :buttons {:display :flex
             :flex-direction :row
             :flex-glow 1
             :margin-top 0
             :margin-bottom "20px"}
   :button {:display :flex
            :justify-content :center
            :align-items :center
            :border-top "1px solid"}
   :button-label {:font-size "16px"
                  :background "#000"
                  :color "#FFF"
                  :padding "5px 10px"}})

(defn root-component []
  (let [styles (make-styles)
        cur-time (get-in @db [:time])
        timer (get-in @db [:timer])
        time-up? (zero? cur-time)
        {:keys [:minutes :seconds]} (sec->min-sec cur-time)]
    [:div {:style (:root-div styles)}
     [:div {:style (:message styles)}
      (gstr/format "%02d:%02d" minutes seconds)]
     [:br]
     [:div {:style (:buttons styles)}
      (for [[on-click label] [[#(toggle-timer!) "Start/Stop"]
                              [#(reset-timer!) "Reset"]]]
        [:div {:key label
               :href "#"
               :on-click (fn [e]
                           (.preventDefault e)
                           (on-click))
               :style (:button styles)}
         [:p {:style (:button-label styles)}
          label]])]]))

(defn ^:dev/after-load start! []
  (rd/render
   [root-component]
   (js/document.getElementById "app-container")))
