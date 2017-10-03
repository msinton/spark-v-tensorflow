
## Matrix Factorization

##### spark

USES ALS: Alternating Least Squares

--- Negatives ---
- Cannot specify the "linear regularization" parameter.
    As such could not get the recommender to perform better than the Popularity recommender.

--- Positives ---
- they **scale the regularization parameter lambda** in solving each least
    squares problem (by the number of ratings the user generated in
    updating user factors, or the number of ratings the product received
    in updating product factors. This approach is named “ALS-WR” and
    discussed in the paper:
    “Large-Scale Parallel Collaborative Filtering for the Netflix Prize” -
     https://link.springer.com/chapter/10.1007%2F978-3-540-68880-8_32)
    = makes lambda less dependent on the scale of the dataset

