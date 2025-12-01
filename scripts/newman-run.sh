#!/usr/bin/env bash
set -euo pipefail

COLLECTION="postman/MathProject.postman_collection.json"
REPORT_DIR="newman"
BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"
USERNAME="${USERNAME:-demo}"
PASSWORD="${PASSWORD:-demo123}"

mkdir -p "$REPORT_DIR"

npx newman@6.2.1 run "$COLLECTION" \
  --iteration-count 100 \
  --concurrency 10 \
  --env-var baseUrl="$BASE_URL" \
  --env-var username="$USERNAME" \
  --env-var password="$PASSWORD" \
  --reporters cli,json \
  --reporter-json-export "$REPORT_DIR/newman-results.json"

node <<'NODE'
const fs = require('fs');
const path = require('path');
const resultsPath = path.join(process.cwd(), 'newman', 'newman-results.json');
const csvPath = path.join(process.cwd(), 'newman', 'newman-results.csv');
const mdPath = path.join(process.cwd(), 'newman', 'comparison-results.md');
const data = JSON.parse(fs.readFileSync(resultsPath));
const rows = [['iteration', 'item', 'status', 'code', 'responseTime']];
data.run.executions.forEach(exec => {
  const res = exec.response || {};
  rows.push([
    exec.cursor?.iteration ?? '',
    exec.item?.name ?? '',
    res.status || '',
    res.code || '',
    res.responseTime || ''
  ]);
});
const toCsv = rows.map(r => r.map(v => '"' + String(v).replace(/"/g, '""') + '"').join(',')).join('\n');
fs.writeFileSync(csvPath, toCsv);
const aggregates = {};
data.run.executions.forEach(exec => {
  const name = exec.item?.name || 'unknown';
  const time = exec.response?.responseTime;
  if (typeof time !== 'number') return;
  if (!aggregates[name]) aggregates[name] = { count: 0, total: 0 };
  aggregates[name].count += 1;
  aggregates[name].total += time;
});
const lines = ['| Endpoint | Avg Response Time (ms) |', '| --- | --- |'];
Object.keys(aggregates).sort().forEach(name => {
  const { count, total } = aggregates[name];
  lines.push(`| ${name} | ${(total / count).toFixed(2)} |`);
});
fs.writeFileSync(mdPath, lines.join('\n'));
NODE

echo "Reports saved to $REPORT_DIR/newman-results.json, $REPORT_DIR/newman-results.csv, and $REPORT_DIR/comparison-results.md"
