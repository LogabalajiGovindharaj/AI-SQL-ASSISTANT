import React from "react";
import "./Landing.css";

export default function Landing({ onGetStarted }) {
  return (
    <div className="landing">
      <div className="landing-content">
        <div className="landing-mark">›_</div>
        <h1 className="landing-title">Ask your database a question.</h1>
        <p className="landing-sub">
          Type a plain-English question. Claude generates the SQL, a
          deterministic Validation Agent checks it before anything touches
          your database, and an Insight Agent explains what came back.
        </p>
        <button className="landing-cta" onClick={onGetStarted}>Get started</button>

        <div className="landing-pipeline">
          {["Understand", "Validate", "Execute", "Insight"].map((step, i) => (
            <React.Fragment key={step}>
              <span className="landing-step">{step}</span>
              {i < 3 && <span className="landing-arrow">→</span>}
            </React.Fragment>
          ))}
        </div>
      </div>
    </div>
  );
}
