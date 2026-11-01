// A random spot in the open Atlantic: harmless on the public map and unique per
// run, so parallel CI runs can't mistake each other's riders for their own.
output.testLat = (0.5 + Math.random()).toFixed(5);
output.testLon = (-26.5 - Math.random()).toFixed(5);
output.position = { found: false };
