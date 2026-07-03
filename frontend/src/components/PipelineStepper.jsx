import React from "react";
import "./PipelineStepper.css";

const STEPS = [
  { key: "understand", label: "Understand" },
  { key: "validate", label: "Validate" },
  { key: "execute", label: "Execute" },
  { key: "insight", label: "Insight" },
];

/**
 * `stage` is one of: idle | understand | validate | rejected | execute | insight | done
 * Encodes the real backend pipeline (Intent+SQL Generation -> Validation ->
 * Execution -> Insight), not a decorative progress bar.
 */
export default function PipelineStepper({ stage }) {
  const activeIndex = STEPS.findIndex((s) => s.key === stage);
  const isRejected = stage === "rejected";
  const isDone = stage === "done";

  return (
    <div className="stepper" role="status" aria-label="Query pipeline progress">
      {STEPS.map((step, i) => {
        let status = "pending";
        if (isRejected && step.key === "validate") status = "rejected";
        else if (isRejected && i > 1) status = "skipped";
        else if (isDone || i < activeIndex) status = "complete";
        else if (i === activeIndex) status = "active";

        return (
          <React.Fragment key={step.key}>
            <div className={`stepper-node stepper-node--${status}`}>
              <span className="stepper-dot" />
              <span className="stepper-label">{step.label}</span>
            </div>
            {i < STEPS.length - 1 && (
              <span className={`stepper-line stepper-line--${status === "complete" ? "complete" : "pending"}`} />
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
}
