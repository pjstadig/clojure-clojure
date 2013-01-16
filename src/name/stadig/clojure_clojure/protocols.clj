;;;; Copyright © 2013 Paul Stadig.  All rights reserved.
;;;;
;;;; The use and distribution terms for this software are covered by the Eclipse
;;;; Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html) which can be
;;;; found in the file epl-v10.html at the root of this distribution.  By using
;;;; this software in any fashion, you are agreeing to be bound by the terms of
;;;; this license.
;;;;
;;;; You must not remove this notice, or any other, from this software.
(ns name.stadig.clojure-clojure.protocols
  (:refer-clojure :only [defprotocol extend-protocol fn]))

(defprotocol Counted
  (count [this])
  (counted? [this]))

(extend-protocol Counted
  Object
  (count [this] (clojure.core/count this))
  (counted? [this] (clojure.core/counted? this)))

(defprotocol IConsable
  (cons [this o]))

(extend-protocol IConsable
  Object
  (cons [this o] (clojure.core/conj this o)))

(defprotocol IEquivable
  (equiv [this o]))

(extend-protocol IEquivable
  Object
  (equiv [this o] (clojure.core/= this o)))

(defprotocol IHashEq
  (hasheq [this]))

(extend-protocol IHashEq
  Object
  (hasheq [this] (clojure.lang.Util/hasheq this)))

(defprotocol IMeta
  (meta [this]))

(extend-protocol IMeta
  Object
  (meta [this] (clojure.core/meta this)))

(defprotocol IObj
  (with-meta [this meta]))

(extend-protocol IObj
  Object
  (with-meta [this meta] (clojure.core/with-meta this meta)))

(defprotocol IPersistentCollection
  (empty [this]))

(extend-protocol IPersistentCollection
  Object
  (empty [this] (clojure.core/empty this)))

(defprotocol IPersistentList
  (list? [this]))

(extend-protocol IPersistentList
  Object
  (list? [this] (clojure.core/list? this)))

(defprotocol IPersistentStack
  (peek [this])
  (pop [this]))

(extend-protocol IPersistentStack
  Object
  (peek [this] (clojure.core/peek this))
  (pop [this] (clojure.core/pop this)))

(defprotocol IReduce
  (reduce [this f] [this f init]))

(extend-protocol IReduce
  Object
  (reduce [this f] (clojure.core/reduce f this))
  (reduce [this f init] (clojure.core/reduce f init this)))

(defprotocol ISeq
  (first [this])
  (more [this])
  (next [this])
  (seq? [this]))

(extend-protocol ISeq
  Object
  (first [this] (clojure.core/first this))
  (more [this] (clojure.core/rest this))
  (next [this] (clojure.core/next this))
  (seq? [this] (clojure.core/seq? this)))

(defprotocol Seqable
  (seq [this]))

(extend-protocol Seqable
  Object
  (seq [this] (clojure.core/seq this)))

(defprotocol Sequential
  (sequential? [this]))

(extend-protocol Sequential
  Object
  (sequential? [this] (clojure.core/sequential? this)))
