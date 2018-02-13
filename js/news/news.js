var width,height
var chartWidth, chartHeight
var margin
var svg = d3.select("#graph").append("svg")
var chartLayer = svg.append("g").classed("chartLayer", true)

    
function generate_visualisation(entity) {
    d3.json('/node_json/' + entity, function(data) {
        d3.select("#title").html(data['labels'][data['entity']])
        
        tabulate(data, ['target', 'topics'])

        setSize(data['conflict_graph'])
        drawChart(data['conflict_graph'])
    })
}

function visualValue(data, column, value) {
    if (column == 'topics') {
        return value.map(function(element) {
            return '<a href="/node/' + element + '">' + data['labels'][element] + '</a>';
        }).join();
    }
    return '<a href="/node/' + value + '">' + data['labels'][value] + '</a>';
}

function tabulate(data, columns) {
    var table = d3.select("#table").append("table").classed("table", true).classed("table-responsive", true),
        thead = table.append("thead"),
        tbody = table.append("tbody");

    // append the header row
    thead.append("tr")
        .selectAll("th")
        .data(columns)
        .enter()
        .append("th")
            .text(function(column) { return column; });

    // create a row for each object in the data
    var rows = tbody.selectAll("tr")
        .data(data['conflict'])
        .enter()
        .append("tr");

    // create a cell in each row for each column
    var cells = rows.selectAll("td")
        .data(function(row) {
            return columns.map(function(column) {
                return {column: column, value: visualValue(data, column, row[column])};
            });
        })
        .enter()
        .append("td")
        .html(function(d) { return d.value; });
    
    return table;
}

function setSize(data) {
    width = document.querySelector("#graph").clientWidth
    height = document.querySelector("#graph").clientHeight
    
    margin = {top:0, left:0, bottom:0, right:0 }
        
        
    chartWidth = width - (margin.left+margin.right)
    chartHeight = height - (margin.top+margin.bottom)
        
    svg.attr("width", width).attr("height", height)
        
        
    chartLayer
        .attr("width", chartWidth)
        .attr("height", chartHeight)
        .attr("transform", "translate("+[margin.left, margin.top]+")")    
}
    
function drawChart(data) {
        
    var simulation = d3.forceSimulation()
        .force("link", d3.forceLink().id(function(d) { return d.index }))
        .force("collide",d3.forceCollide( function(d){return d.r + 30 }).iterations(25) )
        .force("charge", d3.forceManyBody())
        .force("center", d3.forceCenter(chartWidth / 2, chartHeight / 2))
        .force("y", d3.forceY(0))
        .force("x", d3.forceX(0))
    
    svg.append("defs").selectAll("marker")
        .data(["suit", "licensing", "resolved"])
        .enter().append("marker")
        .attr("id", function(d) { return d; })
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 25)
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5 L10,0 L0, -5")
        .style("stroke", "#645394");

    var link = svg.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(data.links)
        .enter()
        .append("line")
        .attr("stroke", "#645394")
        .attr("marker-end", "url(#suit)");
        
    var node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("circle")
        .data(data.nodes)
        .enter().append("circle")
        .attr("r", function(d){  return d.r })
        .attr("fill", "#645394")
        .call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended));

    var label = svg.append("g")
        .attr("class", "labels")
        .selectAll("text")
        .data(data.nodes)
        .enter().append("text")
        .attr("dx", 10)
        .attr("dy", ".35em")
        .attr("font-size", function(d) { return (3 * d.r) + "px"; })
        .text(function(d) { return d.label; }) 
        .style("stroke", "gray")
        .style("fill", "black");
        
    var ticked = function() {
        link
            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });
    
        node
            .attr("cx", function(d) { return d.x; })
            .attr("cy", function(d) { return d.y; });

        label
            .attr("x", function(d) { return d.x; })
            .attr("y", function(d) { return d.y; });
    }  
        
    simulation
        .nodes(data.nodes)
        .on("tick", ticked);
    
    simulation.force("link")
        .links(data.links);    
        
    function dragstarted(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }
        
    function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }
        
    function dragended(d) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    } 
                
}