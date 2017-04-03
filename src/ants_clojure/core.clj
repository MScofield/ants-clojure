(ns ants-clojure.core
  (:require [clojure.java.io :as io])
  (:gen-class :extends javafx.application.Application))

(def width 800)
(def height 600)
(def ant-count 100)

(def ants (atom []))

;;go through all the ants each cycle 
(defn create-ants []
  (for [i (range ant-count)]
    {:x (rand-int width)
     :y (rand-int height)
     :color (javafx.scene.paint.Color/BLACK)
     :mad? false}))


(defn draw-ants! [context]
  (.clearRect context 0 0 width height)
  (doseq [ant @ants]
    (.setFill context (:color ant))
    (.fillOval context (:x ant)(:y ant) 5 5)))
  
(defn random-step []
  (- (* 2 (rand)) 1))


;;randomly wiggle these crazy buggers
(defn move-ant [ant]
  (assoc ant
    :x (+ (random-step) (:x ant))
    :y (+ (random-step) (:y ant))))

;;turn the ants nearest each other red
(defn antitude [ant]
  (Thread/sleep 1)
  (let [crazies
        (filter (fn [a]
                  (and
                       (< (Math/abs (- (:x ant) (:x a))) 17)
                       (< (Math/abs (- (:y ant) (:y a))) 17)))
          @ants)
        crazy-count (count crazies)]
    (assoc ant :color
      (if (> crazy-count 1)
          javafx.scene.paint.Color/RED
        javafx.scene.paint.Color/BLACK))))

;;turn the ants far from any other to green
(defn chill-ant [ant]
  (Thread/sleep 2)
  (let [chillant
          (filter (fn [a]
                    (and
                         (< (Math/abs (- (:x ant) (:x a))) 43)
                         (< (Math/abs (- (:y ant) (:y a))) 43)))
            @ants)
          chill-count (count chillant)]
    (assoc ant :color 
      (if (< chill-count 2)
        javafx.scene.paint.Color/GREEN
        (:color ant)))))

;;calling all the move and color functions
(defn move-ants []
  (doall (pmap chill-ant (pmap antitude (pmap move-ant (deref ants))))))

(def last-timestamp (atom 0))

(defn fps [now]
  (let [diff (- now @last-timestamp)
        diff-seconds (/ diff 1000000000)]
    (int (/ 1 diff-seconds))))

;;build console
(defn -start [app stage]
  (let [root (javafx.fxml.FXMLLoader/load (io/resource "main.fxml"))
        scene (javafx.scene.Scene. root width height)
        canvas (.lookup scene "#canvas")
        context (.getGraphicsContext2D canvas)
        fps-label (.lookup scene "#fps")
        timer (proxy [javafx.animation.AnimationTimer] []
                (handle [now]
                  (.setText fps-label (str (fps now)))
                  (reset! last-timestamp now)
                  (reset! ants (move-ants))
                  (draw-ants! context)))]
    (.setTitle stage "Ants")
    (.setScene stage scene)
    (.show stage)
    (reset! ants (create-ants))
    (.start timer)))

(defn -main []
  (javafx.application.Application/launch ants_clojure.core
    (into-array String [])))
  
