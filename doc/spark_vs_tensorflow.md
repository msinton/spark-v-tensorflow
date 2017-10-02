# Spark vs Tensorflow

#### Usage
![usage header](file://C:\Users\msinton\Documents\research\images\usage_header.png)

![usage](file://C:\Users\msinton\Documents\research\images\tensorflow_spark_usage.png)

source: http://www.kdnuggets.com/2017/05/poll-analytics-data-science-machine-learning-software-leaders.html/2

- comparison of Sparks ML and Tensorflow
    - compare like for like (Spark has limited functionality)
    - compare ease of use
    - performance (to train/ to predict)
    - flexibility
    - setup
    - case study: do something simple using both
    - small vs large - spark overkill for small? (what if resources limited?)
    - learning curve
    - size of models produced
    - results without tuning/ ease of tuning
    - docs

- use tensorflow with spark? Can they complement each other?

### BRAINSTORM steps etc
start with spark. do simple thing like sentiment analysis.
Build up from there.
Install Tensorflow in AWS.

- Jester, recommender for jokes:
https://data.world/galvanize/jester-dataset-case-study

# AWS
login, select "AWSTagAdmin" from shortcut menu top-right

# Setup


# ML notes

#### bias-variance trade-off:
- bias
    - error from erroneous assumption
    - can cause an algorithm to miss the relevant relations between features and target outputs
    - aka underfitting

- variance
    - error from sensitivity to small fluctuations in the training set
    - model the random noise in the training data, rather than the intended outputs
    - aka overfitting

It's helpful to think of these terms under the following condition:
Imagine building multiple models - each time changing slightly the data
that is used in training.
Then **bias** measures how far off on average you are from the correct
"prediction".
Whereas **variance** measures how much the predictions for a given item **vary**.


![bias varaince scatter](file://C:\Users\msinton\Documents\research\images\bias_variance.png)

![bias varaince line](file://C:\Users\msinton\Documents\research\images\biasvariance.png)

- bias–variance decomposition
    - a way of analyzing a learning algorithm's **expected generalization error**
    as a sum of three terms, the bias, variance, and a quantity called
    the **irreducible error**, resulting from noise in the problem itself

- it has been argued that humans favour **high** bias - this allows us to
generalize to a wide variety of problems


## Recommender systems

**Types**
1. content-based recommender (i.e. a typical supervised machine learning recommender)
2. collaborative filtering recommender (probably item-based)
3. matrix factorization recommender
4. hybrid of the above


### matrix factorization recommender

- can easily overfit the data due to the HUGE number of parameters

To solve this:

1. Decrease k (the number of latent factors), and/or
2. Increase lambda (the regularization tuning parameter)

