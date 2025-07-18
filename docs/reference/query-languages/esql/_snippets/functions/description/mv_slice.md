% This is generated by ESQL's AbstractFunctionTestCase. Do not edit it. See ../README.md for how to regenerate it.

**Description**

Returns a subset of the multivalued field using the start and end index values. This is most useful when reading from a function that emits multivalued columns in a known order like [`SPLIT`](/reference/query-languages/esql/functions-operators/string-functions.md#esql-split) or [`MV_SORT`](/reference/query-languages/esql/functions-operators/mv-functions.md#esql-mv_sort).

The order that [multivalued fields](/reference/query-languages/esql/esql-multivalued-fields.md) are read from
underlying storage is not guaranteed. It is **frequently** ascending, but don’t
rely on that.

