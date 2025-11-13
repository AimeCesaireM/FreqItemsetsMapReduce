const flowModes = {
  single: {
    title: "SONMRSingle: Maximum Mapper Parallelism",
    summary:
      "Round two keeps Hadoop's default one-basket-per-record flow. Every mapper validates candidates against a single transaction, emphasizing parallel execution over per-task efficiency.",
    steps: [
      {
        title: "Round One Map",
        description:
          "Custom MultiLineInputFormat feeds fixed-size transaction blocks to the mapper. Each block is mined locally with Apriori to generate candidate itemsets.",
        meta: "Shares RoundOneMapper.java with SONMRMulti.",
      },
      {
        title: "Round One Reduce",
        description:
          "Candidates that meet local support thresholds are aggregated and written to HDFS. Output is published to the distributed cache for the second job.",
        meta: "Output path: intermPath (e.g., /tmp/son/interm).",
      },
      {
        title: "Round Two Map",
        description:
          "Uses Hadoop's default TextInputFormat. Each mapper receives a single transaction, loads cached candidates, and emits matches immediately.",
        meta: "Mapper class: RoundTwoMapperSingle.java.",
      },
      {
        title: "Round Two Reduce",
        description:
          "Combines candidate counts across all mappers to produce globally frequent itemsets, writing the final results to the chosen output directory.",
        meta: "Reducer + combiner reused by both drivers.",
      },
    ],
  },
  multi: {
    title: "SONMRMulti: Micro-Batching Transactions",
    summary:
      "Round two reuses MultiLineInputFormat so each mapper processes a mini-batch of baskets. This reduces mapper spin-up overhead and improves HDFS read efficiency for dense datasets.",
    steps: [
      {
        title: "Round One Map",
        description:
          "Identical to SONMRSingle. Blocks of transactions are mined locally to propose candidate itemsets based on the configured support threshold.",
        meta: "Input sizing controlled via transactions_per_block.",
      },
      {
        title: "Round One Reduce",
        description:
          "Aggregates candidate counts and writes surviving itemsets to the distributed cache for a follow-up validation sweep.",
        meta: "Cache file: intermPath/part-r-00000.",
      },
      {
        title: "Round Two Map",
        description:
          "MultiLineInputFormat batches multiple baskets for each mapper. Candidates are validated against every transaction within the batch before emission.",
        meta: "Mapper class: RoundTwoMapperMulti.java.",
      },
      {
        title: "Round Two Reduce",
        description:
          "Combines counts exactly as in SONMRSingle, returning the globally frequent itemsets along with their support.",
        meta: "Reducer + combiner reused by both drivers.",
      },
    ],
  },
};

const modeButtons = document.querySelectorAll(".mode-toggle__button");
const flowSummary = document.getElementById("flow-summary");
const flowGrid = document.getElementById("flow-grid");

function renderFlow(mode) {
  const config = flowModes[mode];
  if (!config) return;

  flowSummary.querySelector("h3").textContent = config.title;
  flowSummary.querySelector("p").textContent = config.summary;

  flowGrid.innerHTML = "";
  config.steps.forEach((step, index) => {
    const card = document.createElement("article");
    card.className = "flow-card";
    card.innerHTML = `
      <span class="flow-card__step">${index + 1}</span>
      <h4>${step.title}</h4>
      <p>${step.description}</p>
      <div class="flow-card__meta">${step.meta}</div>
    `;
    flowGrid.appendChild(card);
  });
}

modeButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const mode = button.dataset.mode;
    if (!mode) return;

    modeButtons.forEach((btn) => {
      const active = btn === button;
      btn.classList.toggle("is-active", active);
      btn.setAttribute("aria-selected", String(active));
    });

    renderFlow(mode);
  });
});

renderFlow("single");

const datasetInput = document.getElementById("input-dataset");
const blockInput = document.getElementById("input-block");
const frequencyInput = document.getElementById("input-frequency");

const datasetOutput = document.getElementById("output-dataset");
const blockOutput = document.getElementById("output-block");
const frequencyOutput = document.getElementById("output-frequency");

const singleMappers = document.getElementById("single-mappers");
const singleSupport = document.getElementById("single-support");
const multiMappers = document.getElementById("multi-mappers");
const multiData = document.getElementById("multi-data");
const multiSupport = document.getElementById("multi-support");

const numberFormatter = new Intl.NumberFormat("en-US");

function updatePlayground() {
  const datasetSize = Number(datasetInput.value);
  const transactionsPerBlock = Number(blockInput.value);
  const minFreq = Number(frequencyInput.value);

  datasetOutput.textContent = numberFormatter.format(datasetSize);
  blockOutput.textContent = numberFormatter.format(transactionsPerBlock);
  frequencyOutput.textContent = minFreq.toFixed(2);

  const minSupport = Math.floor(minFreq * datasetSize);

  const singleMapperEstimate = datasetSize;
  const multiMapperEstimate = Math.max(
    1,
    Math.ceil(datasetSize / transactionsPerBlock)
  );

  singleMappers.textContent = numberFormatter.format(singleMapperEstimate);
  singleSupport.textContent = numberFormatter.format(minSupport);

  multiMappers.textContent = numberFormatter.format(multiMapperEstimate);
  multiData.textContent = `${numberFormatter.format(
    transactionsPerBlock
  )} baskets`;
  multiSupport.textContent = numberFormatter.format(minSupport);
}

[datasetInput, blockInput, frequencyInput].forEach((input) =>
  input.addEventListener("input", updatePlayground)
);

updatePlayground();
