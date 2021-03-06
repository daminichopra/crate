/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.execution.jobs.kill;

import org.elasticsearch.Version;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.transport.TransportRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class KillJobsRequest extends TransportRequest {

    private final Collection<UUID> toKill;

    @Nullable
    private final String reason;

    public KillJobsRequest(Collection<UUID> jobsToKill, @Nullable String reason) {
        this.toKill = jobsToKill;
        this.reason = reason;
    }

    Collection<UUID> toKill() {
        return toKill;
    }

    public KillJobsRequest(StreamInput in) throws IOException {
        super(in);
        int numJobs = in.readVInt();
        toKill = new ArrayList<>(numJobs);
        for (int i = 0; i < numJobs; i++) {
            UUID job = new UUID(in.readLong(), in.readLong());
            toKill.add(job);
        }
        if (in.getVersion().onOrAfter(Version.V_4_1_0)) {
            reason = in.readOptionalString();
        } else {
            reason = null;
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        int numJobs = toKill.size();
        out.writeVInt(numJobs);
        for (UUID job : toKill) {
            out.writeLong(job.getMostSignificantBits());
            out.writeLong(job.getLeastSignificantBits());
        }
        if (out.getVersion().onOrAfter(Version.V_4_1_0)) {
            out.writeOptionalString(reason);
        }
    }

    @Override
    public String toString() {
        return "KillJobsRequest{" + toKill + '}';
    }

    @Nullable
    public String reason() {
        return reason;
    }
}
