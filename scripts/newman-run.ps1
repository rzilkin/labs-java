$collection = "postman/MathProject.postman_collection.json"
$reportDir = "newman"
$baseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080/api/v1" }
$username = if ($env:USERNAME) { $env:USERNAME } else { "demo" }
$password = if ($env:PASSWORD) { $env:PASSWORD } else { "demo123" }

New-Item -ItemType Directory -Force -Path $reportDir | Out-Null

npx newman@6.2.1 run $collection `
  --iteration-count 100 `
  --concurrency 10 `
  --env-var baseUrl=$baseUrl `
  --env-var username=$username `
  --env-var password=$password `
  --reporters cli,json `
  --reporter-json-export "$reportDir/newman-results.json"

node -Command @"
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
"@

Write-Host "Reports saved to $reportDir/newman-results.json, $reportDir/newman-results.csv, and $reportDir/comparison-results.md"
