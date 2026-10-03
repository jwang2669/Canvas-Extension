# Research Report

## MySQL Schema

### Summary of Work

In order to draft what our DB schema would look like, I had to first look into what accessing data from Canvas would look like. I looked at Canvas' API docs[^1] and read through the 'Resources' page to get a sense of what tables we will want to access and how often. I then began to think about what data we will want to 'cache' in our DB and what data we will want to request from the API anytime we need it. Below 'Results' I have listed my notes and considerations from what I've researched as well as the tables I believe we should start out with.

### Motivation

I have database knowledge from CS564, however I needed to research the data we will be accessing and creating before designing a database that fits our needs.

### Time Spent

~ 2 hours

### Results

Our web extension will often need to access data from Canvas, but we should try to minimize the requests we send. We do this by caching some of the data we get from Canvas to our database. Still, we don't want to cache too much data because a lot of this data will also be changing.

Additionally, we will be creating our own data as our users interact with the web extension and change settings and what not.

So, to begin, I believe the following tables will be needed (to start):

- ooc_user --> we keep unique ids for each user of OOC
- canvas_instance --> users of different schools will have different base URLs that we'll need to keep track of
- canvas_connection --> here we map ooc users to canvas accounts and keep track of authentication creds and access tokens here. we use this to make API calls and enforce perms.
- course_cache --> we cache the courses that our user is enrolled in, enrollment doesn't frequently change so we can cache this
- assignment_cache --> our extension relies extensively on assingment data, so we cache this and refresh the cache fairly often. we cache this so we don't have to constantly request the API.
- assignment_additions --> here we store what we add to assignments (summaries, completion estimates, etc.)
- todo_item --> self explanatory, we keep track of upcoming/overdue assignments that user should keep track of. here they can see a list of assignments across their courses in one list.

We may add more tables as we get the project moving in the next week or so. As we add features we may need other tables. I wanted to keep the number of tables minimal since it can get messy if we do too much too soon.

I'm not sure how often we'll be requesting from the Canvas API, but I've linked the rate limits[^2] in sources so we can keep that in mind going forward.

### Sources

- Canvas API docs[^1]
- rate limits[^2]

[^1]: https://developerdocs.instructure.com/services/canvas/resources

[^2]: https://developerdocs.instructure.com/services/dap/limits-policies#rate-limits
