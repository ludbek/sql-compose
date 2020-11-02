# sql-compose

```
sql-compose "0.1.0"
```

## Why yet another query generator?

Composing a sql query does not need to be complex.
`sql-compose` has single abstraction, thats it.

The idea behind `sql-compose` is to replace dynamic parts of
the queries with functions. While working with `sql-compose`
we mostly write plain sql statements.

## An example of using sql-compose

```clojure
(require '[sql-compose.core :refer [sql where]])

(sql "SELECT * FROM users"
     "INNER JOIN sessions ON sessions.user_id = users.id"
     (where {:users.id 123 :users.active true}))

; ["SELECT * FROM users INNER JOIN sessions ON sessions.user_id = users.id WHERE users.id = ? AND users.active = ?" 123 true]
```

The interesting part here is `where`. Its just a normal function
that gets `placeholder generator` as an argument.

Lets implement a simple form of `where` here. This will give us
an idea of how we can construct the dynamic parts of queries
when we need to.

```clojure
(defn where [id]
  (fn [gen-placeholder]
   (str "WHERE id =" (gen-placeholder id))))

(sql "SELECT * FROM users"
     (where 123))

; ["SELECT * FROM users WHERE id = ?" 123]
```

## Fillers

We will call these dynamic parts `fillers`.

`sql-compose` packs some common `fillers`.

### where
When a map is passed to `where`, it uses `and`.

```clojure
=> (require '[sql-compose.core :refer [sql where]])

=> (sql "SELECT * FROM users"
     (where {:id 1 :active true}))

["SELECT * FROM users WHERE (id = ? AND active = ?)" 1 true]
```

For complex logics we can use `AND` and `OR` fillers.

```clojure
=> (require '[sql-compose.core :refer [sql where AND OR]])

=> (sql "SELECT * FROM users"
        (where (AND ["id =" 1]
                    (OR ["active =" true]
		        ["city =" "kathmandu"]))))

["SELECT * FROM users WHERE (id = ? AND (active = ? OR city = ?))" 1 true "kathmandu"]
```

### insert-into
`insert-into` can be used to insert single or multiple rows of data.

#### Insert single data
```clojure
=> (require '[sql-compose.core :refer [sql insert-into]])

=> (sql (insert-into :users {:email "someone@example.com" :city "kathmandu" }))

["INSERT INTO users (email, city) VALUES (?, ?)" "someone@example.com" "kathmandu"]
```

#### Insert multiple data
```clojure
=> (require '[sql-compose.core :refer [sql insert-into]])

=> (sql (insert-into :users [{:email "someone@example.com" :city "kathmandu" }
                             {:email "anotherone@example.com" :city "sydney"}]))

["INSERT INTO users (email, city) VALUES (?, ?), (?, ?)" "someone@example.com" "kathmandu" "anotherone@example.com" "sydney"]
```

### set-values
`set-values` is a filler for updating rows.

```clojure
=> (require '[sql-compose.core :refer [sql set-values where]])

=> (sql "UPDATE users"
        (set-values {:email "someone@example.com"})
	(where {:id 1}))

["UPDATE users SET email = ? WHERE (id = ?)" "someone@example.com" 1]
```


## License

Copyright © 2020 suren

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
