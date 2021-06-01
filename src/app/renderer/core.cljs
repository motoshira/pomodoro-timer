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

(def ^:private init-time (* 60 30))

(defonce ^:private db
  (r/atom {:timer nil
           :time init-time}))

;;;
;;; events
;;;

(defn- dec-time! []
    (swap! db update-in [:time] #(max 0 (- % 1))))

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
      (swap! db
             assoc :timer
             (set-interval!)))
    (do
      (clear-interval! (:timer @db))
      (swap! db assoc :timer nil))))

(defn- reset-timer! []
  (when-let [timer (get @db :timer)]
    (clear-interval! timer))
  (swap! db assoc :timer nil)
  (swap! db assoc :time init-time))


;;;
;;; subs
;;;

(defn- sec->min-sec [seconds]
  {:minutes (quot seconds 60)
   :seconds (rem seconds 60)})

;;;
;;; views
;;;

(def ^:private styles
  (let [basic-attrs {:height "520px"
                     :width "320px"
                     :overflow :visible
                     :display :flex
                     :flex-direction :column
                     :justify-content :center
                     :align-items :center
                     :background "#000"
                     :color "#FFF"
                     "&::-webkit-scrollbar" {:display :none}}]
    {:root-div basic-attrs
     :root-div-transparent (assoc basic-attrs
                                  :background "rgba(0, 0, 0, 0.9)")
     :message {:flex-glow 1}
     :buttons {:display :flex
               :flex-glow 1
               :padding-top "20px"}
     :button {:background "#000"
              :color "#FFF"}}))

(defn root-component []
  (let [cur-time (get-in @db [:time])
        timer (get-in @db [:timer])
        time-up? (zero? cur-time)
        root-style (get styles
                        (if (or time-up?
                                (nil? timer))
                          :root-div
                          :root-div-transparent))
        {:keys [:minutes :seconds]} (sec->min-sec cur-time)]
    [:div {:style (:root-div styles)}
     (gstr/format "Time is now: %02d:%02d" minutes seconds)
     [:br]
     [:div {:style (:buttons styles)}
      (for [[on-click label] [[#(toggle-timer!) "Start/Stop"]
                              [#(reset-timer!) "Reset"]]]
        [:button {:key label
                  :on-click on-click
                  :style (:button styles)}
         label])]]))

(defn ^:dev/after-load start! []
  (rd/render
   [root-component]
   (js/document.getElementById "app-container")))
