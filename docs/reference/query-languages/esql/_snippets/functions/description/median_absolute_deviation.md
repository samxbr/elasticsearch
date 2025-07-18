% This is generated by ESQL's AbstractFunctionTestCase. Do not edit it. See ../README.md for how to regenerate it.

**Description**

Returns the median absolute deviation, a measure of variability. It is a robust statistic, meaning that it is useful for describing data that may have outliers, or may not be normally distributed. For such data it can be more descriptive than standard deviation.  It is calculated as the median of each data point’s deviation from the median of the entire sample. That is, for a random variable `X`, the median absolute deviation is `median(|median(X) - X|)`.

::::{note}
Like [`PERCENTILE`](/reference/query-languages/esql/functions-operators/aggregation-functions.md#esql-percentile), `MEDIAN_ABSOLUTE_DEVIATION` is [usually approximate](/reference/query-languages/esql/functions-operators/aggregation-functions.md#esql-percentile-approximate).
::::


