/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.core.group;

import static com.querydsl.core.group.GroupBy.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import com.google.common.collect.Ordering;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;

public class GroupByMapTest extends AbstractGroupByTest {

    @Test
    public void Compile() {
        StringExpression str = Expressions.stringPath("str");
        GroupExpression<String, String> strGroup = new GOne<String>(str);
        GroupBy.sortedMap(strGroup, str, null);
        GroupBy.sortedMap(str, strGroup, null);
    }

    @Test
    public void Group_Order() {
        Map<Integer, Group> results = BASIC_RESULTS
            .transform(groupBy(postId).as(postName, set(commentId)));
        assertEquals(4, results.size());
    }

    @Test
    public void Set_By_Sorted() {
        Map<Integer, Group> results = BASIC_RESULTS_UNORDERED
                .transform(groupBy(postId).as(postName, sortedSet(commentId)));

        Group group = results.get(1);
        Iterator<Integer> it = group.getSet(commentId).iterator();
        assertEquals(1, it.next().intValue());
        assertEquals(2, it.next().intValue());
        assertEquals(3, it.next().intValue());
    }

    @Test
    public void Set_By_Sorted_Reverse() {
        Map<Integer, Group> results = BASIC_RESULTS_UNORDERED
                .transform(groupBy(postId).as(postName, sortedSet(commentId, Ordering.natural().reverse())));

        Group group = results.get(1);
        Iterator<Integer> it = group.getSet(commentId).iterator();
        assertEquals(3, it.next().intValue());
        assertEquals(2, it.next().intValue());
        assertEquals(1, it.next().intValue());
    }

    @Test
    public void First_Set_And_List() {
        Map<Integer, Group> results = BASIC_RESULTS.transform(
            groupBy(postId).as(postName, set(commentId), list(commentText)));

        Group group = results.get(1);
        assertEquals(toInt(1), group.getOne(postId));
        assertEquals("post 1", group.getOne(postName));
        assertEquals(toSet(1, 2, 3), group.getSet(commentId));
        assertEquals(Arrays.asList("comment 1", "comment 2", "comment 3"), group.getList(commentText));
    }

    @Test
    public void Group_By_Null() {
        Map<Integer, Group> results = BASIC_RESULTS.transform(
            groupBy(postId).as(postName, set(commentId), list(commentText)));

        Group group = results.get(null);
        assertNull(group.getOne(postId));
        assertEquals("null post", group.getOne(postName));
        assertEquals(toSet(7, 8), group.getSet(commentId));
        assertEquals(Arrays.asList("comment 7", "comment 8"), group.getList(commentText));

    }

    @Test(expected = NoSuchElementException.class)
    public void NoSuchElementException() {
        Map<Integer, Group> results = BASIC_RESULTS.transform(
            groupBy(postId).as(postName, set(commentId), list(commentText)));

        Group group = results.get(1);
        group.getSet(qComment);
    }

    @Test(expected = ClassCastException.class)
    public void ClassCastException() {
        Map<Integer, Group> results = BASIC_RESULTS.transform(
            groupBy(postId).as(postName, set(commentId), list(commentText)));

        Group group = results.get(1);
        group.getList(commentId);
    }

    @Test
    public void Map() {
        Map<Integer, Group> results = MAP_RESULTS.transform(
            groupBy(postId).as(postName, map(commentId, commentText)));

        Group group = results.get(1);

        Map<Integer, String> comments = group.getMap(commentId, commentText);
        assertEquals(3, comments.size());
        assertEquals("comment 2", comments.get(2));
    }

    @Test
    public void Map_Sorted() {
        Map<Integer, Group> results = MAP_RESULTS.transform(
                groupBy(postId).as(postName, sortedMap(commentId, commentText)));

        Group group = results.get(1);

        Iterator<Map.Entry<Integer, String>> it = group.getMap(commentId, commentText).entrySet().iterator();
        assertEquals(1, it.next().getKey().intValue());
        assertEquals(2, it.next().getKey().intValue());
        assertEquals(3, it.next().getKey().intValue());
    }

    @Test
    public void Map_Sorted_Reverse() {
        Map<Integer, Group> results = MAP_RESULTS.transform(
                groupBy(postId).as(postName, sortedMap(commentId, commentText, Ordering.natural().reverse())));

        Group group = results.get(1);

        Iterator<Map.Entry<Integer, String>> it = group.getMap(commentId, commentText).entrySet().iterator();
        assertEquals(3, it.next().getKey().intValue());
        assertEquals(2, it.next().getKey().intValue());
        assertEquals(1, it.next().getKey().intValue());
    }


    @Test
    public void Map2() {
        Map<Integer, Map<Integer, String>> results = MAP2_RESULTS.transform(
            groupBy(postId).as(map(commentId, commentText)));

        Map<Integer, String> comments = results.get(1);
        assertEquals(3, comments.size());
        assertEquals("comment 2", comments.get(2));
    }

    @Test
    public void Map3() {
        Map<Integer, Map<Integer, Map<Integer, String>>> actual = MAP3_RESULTS.transform(
            groupBy(postId).as(map(postId, map(commentId, commentText))));

        Map<Integer, Map<Integer, Map<Integer, String>>> expected = new LinkedHashMap<Integer, Map<Integer, Map<Integer, String>>>();
        for (Iterator<Tuple> iterator = MAP3_RESULTS.iterate(); iterator.hasNext();) {
            Tuple tuple = iterator.next();
            Object[] array = tuple.toArray();

            Map<Integer, Map<Integer, String>> posts = expected.get(array[0]);
            if (posts == null) {
                posts = new LinkedHashMap<Integer, Map<Integer,String>>();
                expected.put((Integer) array[0], posts);
            }
            @SuppressWarnings("unchecked")
            Pair<Integer, Pair<Integer, String>> pair = (Pair<Integer, Pair<Integer, String>>) array[1];
            Integer first = pair.getFirst();
            Map<Integer, String> comments = posts.get(first);
            if (comments == null) {
                comments = new LinkedHashMap<Integer, String>();
                posts.put(first, comments);
            }
            Pair<Integer, String> second = pair.getSecond();
            comments.put(second.getFirst(), second.getSecond());
        }
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void Map4() {
        Map<Integer, Map<Map<Integer, String>, String>> actual = MAP4_RESULTS.transform(
            groupBy(postId).as(map(map(postId, commentText), postName)));

        Map<Integer, Map<Map<Integer, String>, String>> expected = new LinkedHashMap<Integer, Map<Map<Integer, String>, String>>();
        for (Iterator<Tuple> iterator = MAP4_RESULTS.iterate(); iterator.hasNext();) {
            Tuple tuple = iterator.next();
            Object[] array = tuple.toArray();

            Map<Map<Integer, String>, String> comments = expected.get(array[0]);
            if (comments == null) {
                comments = new LinkedHashMap<Map<Integer, String>, String>();
                expected.put((Integer) array[0], comments);
            }
            @SuppressWarnings("unchecked")
            Pair<Pair<Integer, String>, String> pair = (Pair<Pair<Integer, String>, String>) array[1];
            Pair<Integer, String> first = pair.getFirst();
            Map<Integer, String> posts = Collections.singletonMap(first.getFirst(), first.getSecond());
            comments.put(posts, pair.getSecond());
        }
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void Array_Access() {
        Map<Integer, Group> results = BASIC_RESULTS.transform(
            groupBy(postId).as(postName, set(commentId), list(commentText)));

        Group group = results.get(1);
        Object[] array = group.toArray();
        assertEquals(toInt(1), array[0]);
        assertEquals("post 1", array[1]);
        assertEquals(toSet(1, 2, 3), array[2]);
        assertEquals(Arrays.asList("comment 1", "comment 2", "comment 3"), array[3]);
    }

    @Test
    public void Transform_Results() {
        Map<Integer, Post> results = POST_W_COMMENTS.transform(
                groupBy(postId).as(Projections.constructor(Post.class, postId, postName, set(qComment))));

        Post post = results.get(1);
        assertNotNull(post);
        assertEquals(toInt(1), post.getId());
        assertEquals("post 1", post.getName());
        assertEquals(toSet(comment(1), comment(2), comment(3)), post.getComments());
    }

    @Test
    public void Transform_Via_GroupByProjection() {
        Map<Integer, Post> results = POST_W_COMMENTS2.transform(
                new GroupByProjection<Integer, Post>(postId, postName, set(qComment)) {
                    @Override
                    protected Post transform(Group group) {
                        return new Post(
                                group.getOne(postId),
                                group.getOne(postName),
                                group.getSet(qComment));

                    }
                });

        Post post = results.get(1);
        assertNotNull(post);
        assertEquals(toInt(1), post.getId());
        assertEquals("post 1", post.getName());
        assertEquals(toSet(comment(1), comment(2), comment(3)), post.getComments());
    }

    @Test
    public void Transform_As_Bean() {
        Map<Integer, Post> results = POST_W_COMMENTS.transform(
                groupBy(postId).as(Projections.bean(Post.class, postId, postName, set(qComment).as("comments"))));

        Post post = results.get(1);
        assertNotNull(post);
        assertEquals(toInt(1), post.getId());
        assertEquals("post 1", post.getName());
        assertEquals(toSet(comment(1), comment(2), comment(3)), post.getComments());
    }


    @Test
    public void OneToOneToMany_Projection() {
        Map<String, User> results = USERS_W_LATEST_POST_AND_COMMENTS.transform(
            groupBy(userName).as(Projections.constructor(User.class, userName,
                Projections.constructor(Post.class, postId, postName, set(qComment)))));

        assertEquals(2, results.size());

        User user = results.get("Jane");
        Post post = user.getLatestPost();
        assertEquals(toInt(2), post.getId());
        assertEquals("post 2", post.getName());
        assertEquals(toSet(comment(4), comment(5)), post.getComments());
    }

    @Test
    public void OneToOneToMany_Projection_As_Bean() {
        Map<String, User> results = USERS_W_LATEST_POST_AND_COMMENTS.transform(
            groupBy(userName).as(Projections.bean(User.class, userName,
                Projections.bean(Post.class, postId, postName, set(qComment).as("comments")).as("latestPost"))));

        assertEquals(2, results.size());

        User user = results.get("Jane");
        Post post = user.getLatestPost();
        assertEquals(toInt(2), post.getId());
        assertEquals("post 2", post.getName());
        assertEquals(toSet(comment(4), comment(5)), post.getComments());
    }

    @Test
    public void OneToOneToMany_Projection_As_Bean_And_Constructor() {
        Map<String, User> results = USERS_W_LATEST_POST_AND_COMMENTS.transform(
            groupBy(userName).as(Projections.bean(User.class, userName,
                Projections.constructor(Post.class, postId, postName, set(qComment)).as("latestPost"))));

        assertEquals(2, results.size());

        User user = results.get("Jane");
        Post post = user.getLatestPost();
        assertEquals(toInt(2), post.getId());
        assertEquals("post 2", post.getName());
        assertEquals(toSet(comment(4), comment(5)), post.getComments());
    }

}
