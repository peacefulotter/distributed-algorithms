#!/usr/bin/env python3

import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--proc",
        required=True,
        dest="proc",
        help="Total number of processes",
    )

    parser.add_argument(
        "--debug",
        required=False,
        dest="debug",
        help="Debug mode",
    )

    results = parser.parse_args()
    procs = int(results.proc)
    debug = bool(results.debug)

    proposals = []
    for i in range(1, procs + 1):
        file = f"./configs/lattice-agreement-{i:d}.config"
        with open(file) as f:
            proposals.append( [set(line.rstrip().split(" ")) for line in f][1:] )

    decisions = []
    for i in range(1, procs + 1):
        file = f"./output/{i:d}.output"
        with open(file) as f:
            decisions.append( [set(line.rstrip().split(" ")) for line in f] )

    for i in range(len(decisions[0])):
        total_proposal = set(())
        for proposal in proposals:
            tokens = proposal[i]
            for token in tokens:
                total_proposal.add(token)

        if debug:
            print( "Checking line: ", i, ", total: ", total_proposal )

        for j in range(len(decisions)):
            proposal = proposals[j][i]
            decision = decisions[j][i]
            val1 = proposal.issubset(decision)
            val2 = decision.issubset(total_proposal)

            if debug:
                print(" - ", j, proposal, decision, val1, val2)

            if not val1 or not val2:
                print(" - - Breaks validity")

            for k in range(len(decisions)):
                dec2 = decisions[k][i]
                if not decision.issubset(dec2) and not dec2.issubset(decision):
                    print(" - - Breaks consistency with ", k)

