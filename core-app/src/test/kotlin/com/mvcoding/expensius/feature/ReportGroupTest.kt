/*
 * Copyright (C) 2016 Mantas Varnagiris.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.mvcoding.expensius.feature

import com.mvcoding.expensius.extensions.splitIntoGroupIntervals
import com.mvcoding.expensius.extensions.toInterval
import com.mvcoding.expensius.extensions.toNumberOfGroups
import com.mvcoding.expensius.extensions.toPeriod
import com.mvcoding.expensius.model.ReportGroup
import com.mvcoding.expensius.model.ReportGroup.DAY
import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period
import org.junit.Assert.assertThat
import org.junit.Test

class ReportGroupTest {
    @Test
    fun numberOfStepsIs0WhenIntervalIsEmptyOrDoesNotFillThePeriod() {
        val anyDate = DateTime()
        val emptyInterval = Interval(0, 0)

        ReportGroup.values().forEach {
            assertThat(it.toNumberOfGroups(emptyInterval), equalTo(0))
            assertThat(
                    "$it",
                    it.toNumberOfGroups(Interval(anyDate, it.toPeriod().minusMinutes(1))),
                    equalTo(0))
        }
    }

    @Test
    fun numberOfStepsIs1WhenIntervalIsSameAsStep() {
        val anyDate = DateTime()

        ReportGroup.values().forEach {
            assertThat("$it", it.toNumberOfGroups(Interval(anyDate, it.toPeriod())), equalTo(1))
        }
    }

    @Test
    fun numberOfStepsIs1WhenIntervalDoesNotFillTheSecondPeriod() {
        val anyDate = DateTime()

        ReportGroup.values().forEach {
            assertThat("$it", it.toNumberOfGroups(Interval(anyDate, it.toPeriod().plusMinutes(1))), equalTo(1))
        }
    }

    @Test
    fun intervalWrapsGivenTimestamp() {
        val now = DateTime.now()
        val timestamp = now.millis

        ReportGroup.values().forEach {
            val interval = it.toInterval(timestamp)
            val expectedInterval = when (it) {
                DAY -> Interval(now.withTimeAtStartOfDay(), Period.days(1))
            }
            assertThat("$it", interval, equalTo(expectedInterval))
        }
    }

    @Test
    fun splitsIntoIntervalsThatFullyCoverGivenIntervalWhenItCanBeDividedInEqualPeriods() {
        val timestamp = DateTime.now().millis

        ReportGroup.values().forEach {
            val firstInterval = it.toInterval(timestamp)
            val secondInterval = firstInterval
                    .withStart(firstInterval.end)
                    .withPeriodAfterStart(it.toPeriod())
            val totalInterval = Interval(firstInterval.start, secondInterval.end)
            val splitIntervals = it.splitIntoGroupIntervals(totalInterval)

            assertThat("$it", splitIntervals, equalTo(listOf(firstInterval, secondInterval)))
        }
    }

    @Test
    fun returnsEmptyListWhenIntervalIsSmallerThanPeriod() {
        val timestamp = DateTime.now().millis

        ReportGroup.values().forEach {
            val interval = it.toInterval(timestamp)
            val totalInterval = Interval(
                    interval.start.plusMinutes(1),
                    interval.end.minusMinutes(1))
            val splitIntervals = it.splitIntoGroupIntervals(totalInterval)

            assertThat("$it", splitIntervals, equalTo(emptyList()))
        }
    }

    @Test
    fun splitsIntoIntervalsByCuttingOutEdgesWhenCannotBeDividedInEqualPeriods() {
        val timestamp = DateTime.now().millis

        ReportGroup.values().forEach {
            val firstInterval = it.toInterval(timestamp)
            val secondInterval = firstInterval
                    .withStart(firstInterval.end)
                    .withPeriodAfterStart(it.toPeriod())
            val totalInterval = Interval(
                    firstInterval.start.minusMinutes(1),
                    secondInterval.end.plusMinutes(1))
            val splitIntervals = it.splitIntoGroupIntervals(totalInterval)

            assertThat("$it", splitIntervals, equalTo(listOf(firstInterval, secondInterval)))
        }
    }
}