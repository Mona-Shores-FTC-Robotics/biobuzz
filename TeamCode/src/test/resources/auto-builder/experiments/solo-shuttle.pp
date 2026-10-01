{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-l-exit-1",
      "color": "#3cc8e4",
      "name": "START to L_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-home-2",
      "color": "#3cc8e4",
      "name": "L_EXIT to L_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-far-flower-3",
      "color": "#3cc8e4",
      "name": "L_HOME to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.4,
        "y": 127.8
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-l-home-4",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to L_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-r-exit-5",
      "color": "#3cc8e4",
      "name": "L_HOME to R_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-home-6",
      "color": "#3cc8e4",
      "name": "R_EXIT to R_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-garden-in-7",
      "color": "#3cc8e4",
      "name": "R_HOME to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 22
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 22
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-garden-8",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-r-home-9",
      "color": "#3cc8e4",
      "name": "GARDEN to R_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 10
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 18
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-exit-10",
      "color": "#3cc8e4",
      "name": "R_HOME to L_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-home-11",
      "color": "#3cc8e4",
      "name": "L_EXIT to L_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-l-exit-1"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-2"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-3"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-4"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-5"
    },
    {
      "kind": "path",
      "lineId": "to-r-home-6"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-7"
    },
    {
      "kind": "path",
      "lineId": "to-garden-8"
    },
    {
      "kind": "path",
      "lineId": "to-r-home-9"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-10"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-11"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18,
    "rHeight": 18,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 45.0,
    "maxDeceleration": 45.0,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 262,
    "rightPanelWidth": 714,
    "headingArrowLength": 50,
    "headingArrowColor": "#ffffff",
    "headingArrowThickness": 2,
    "pathOpacity": 1,
    "leftPanelMinWidth": 0,
    "rightPanelMinWidth": 0,
    "penToolMaxPaths": 8,
    "curveThroughMaxPoints": 4,
    "experimentalFeatures": {
      "optimize": false,
      "curveThrough": false
    }
  },
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "exportName": "solo-shuttle",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll"
      ],
      "conditions": [
        "Tip",
        "IntakeFull",
        "RightCellUp",
        "Empty",
        "LeftCellUp"
      ],
      "typicalS": {
        "LaunchOne": 0.5,
        "LaunchAll": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "R_HOME": [
        57.5,
        10,
        90
      ],
      "L_HOME": [
        57.5,
        131.75,
        270
      ],
      "R_EXIT": [
        57.5,
        34,
        90
      ],
      "L_EXIT": [
        57.5,
        108,
        90
      ],
      "FAR_FLOWER": [
        47.4,
        127.8,
        90
      ],
      "GARDEN_IN": [
        8.5,
        22,
        270
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ]
    },
    "pathEnds": {
      "to-l-exit-1": "L_EXIT",
      "to-l-home-2": "L_HOME",
      "to-far-flower-3": "FAR_FLOWER",
      "to-l-home-4": "L_HOME",
      "to-r-exit-5": "R_EXIT",
      "to-r-home-6": "R_HOME",
      "to-garden-in-7": "GARDEN_IN",
      "to-garden-8": "GARDEN",
      "to-r-home-9": "R_HOME",
      "to-l-exit-10": "L_EXIT",
      "to-l-home-11": "L_HOME"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-4",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-5",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-6",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "TIP 1?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 2000,
            "cards": [
              {
                "id": "a-1",
                "kind": "action",
                "name": "LaunchOne"
              },
              {
                "id": "w-2",
                "kind": "firstOf",
                "label": "TIP 1 (4th POLLEN)",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1500,
                    "cards": []
                  }
                ]
              }
            ],
            "label": "No"
          }
        ]
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Catch the TIP 1 spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2200,
            "cards": []
          }
        ]
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-l-exit-1",
        "park": false
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-l-home-2",
        "park": false
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "Fire at the left CELL",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-15",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 1200,
            "cards": [
              {
                "id": "p-10",
                "kind": "path",
                "lineId": "to-far-flower-3",
                "park": false
              },
              {
                "id": "w-11",
                "kind": "firstOf",
                "label": "Collect at the FLOWER",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1800,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-12",
                "kind": "path",
                "lineId": "to-l-home-4",
                "park": false
              },
              {
                "id": "w-13",
                "kind": "firstOf",
                "label": "Fire again (TIP 2)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: the FLOWER"
          }
        ]
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "Catch the TIP 2 spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2200,
            "cards": []
          }
        ]
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-r-exit-5",
        "park": false
      },
      {
        "id": "p-18",
        "kind": "path",
        "lineId": "to-r-home-6",
        "park": false
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "Fire at the right CELL",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-25",
        "kind": "firstOf",
        "label": "TIP 3?",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes"
          },
          {
            "afterMs": 1200,
            "cards": [
              {
                "id": "p-19",
                "kind": "path",
                "lineId": "to-garden-in-7",
                "park": false
              },
              {
                "id": "p-20",
                "kind": "path",
                "lineId": "to-garden-8",
                "park": false
              },
              {
                "id": "w-21",
                "kind": "firstOf",
                "label": "Collect in the GARDEN",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1500,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-22",
                "kind": "path",
                "lineId": "to-r-home-9",
                "park": false
              },
              {
                "id": "w-23",
                "kind": "firstOf",
                "label": "Fire again (TIP 3)",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: the GARDEN"
          }
        ]
      },
      {
        "id": "w-26",
        "kind": "firstOf",
        "label": "Catch the TIP 3 spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2200,
            "cards": []
          }
        ]
      },
      {
        "id": "p-27",
        "kind": "path",
        "lineId": "to-l-exit-10",
        "park": false
      },
      {
        "id": "p-28",
        "kind": "path",
        "lineId": "to-l-home-11",
        "park": false
      },
      {
        "id": "w-29",
        "kind": "firstOf",
        "label": "Fire at the left CELL (TIP 4)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "Pick up what TIP 2 left",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-31",
        "kind": "firstOf",
        "label": "Fire until it tips (TIP 4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      }
    ]
  },
  "version": "1.5.0"
}