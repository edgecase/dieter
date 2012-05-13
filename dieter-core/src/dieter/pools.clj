(ns dieter.pools)

;;; Pool of scopes, to be used one per preprocessor

(defn make-pool []
  (ref #{}))

(defn remove-from-pool [pool item]
  (alter pool disj item)
  item)

(defn get-from-pool [pool initializer]
  (let [a (atom nil)]
    (dosync
     (when-let [item (first @pool)]
       (swap! a (constantly item))
       (remove-from-pool pool item)))
    (if @a
      @a
      (initializer))))

(defn add-to-pool [pool item]
  (dosync
   (alter pool conj item)))

(defmacro with-pool [pool name initializer & body]
  `(let [~name (get-from-pool ~pool ~initializer)]
     (try
       (do ~@body)
       (finally (add-to-pool ~pool ~name)))))
