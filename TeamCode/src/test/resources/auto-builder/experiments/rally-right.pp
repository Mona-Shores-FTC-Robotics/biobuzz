{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-home-r-1",
      "color": "#3cc8e4",
      "name": "START to HOME_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-r-2",
      "color": "#3cc8e4",
      "name": "HOME_R to SWEEP_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-home-r-3",
      "color": "#3cc8e4",
      "name": "SWEEP_R to HOME_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-r-4",
      "color": "#3cc8e4",
      "name": "HOME_R to SWEEP_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-home-r-5",
      "color": "#3cc8e4",
      "name": "SWEEP_R to HOME_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-r-6",
      "color": "#3cc8e4",
      "name": "HOME_R to SWEEP_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-home-r-7",
      "color": "#3cc8e4",
      "name": "SWEEP_R to HOME_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-sweep-r-8",
      "color": "#3cc8e4",
      "name": "HOME_R to SWEEP_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-home-r-9",
      "color": "#3cc8e4",
      "name": "SWEEP_R to HOME_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
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
      "lineId": "to-home-r-1"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-r-2"
    },
    {
      "kind": "path",
      "lineId": "to-home-r-3"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-r-4"
    },
    {
      "kind": "path",
      "lineId": "to-home-r-5"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-r-6"
    },
    {
      "kind": "path",
      "lineId": "to-home-r-7"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-r-8"
    },
    {
      "kind": "path",
      "lineId": "to-home-r-9"
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
    "exportName": "rally-right",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll"
      ],
      "conditions": [
        "LeftCellUp",
        "IntakeFull",
        "RightCellUp",
        "Empty"
      ],
      "typicalS": {
        "LaunchOne": 0.5,
        "LaunchAll": 2.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "HOME_R": [
        59,
        10,
        90
      ],
      "SWEEP_R": [
        45,
        10,
        90
      ]
    },
    "pathEnds": {
      "to-home-r-1": "HOME_R",
      "to-sweep-r-2": "SWEEP_R",
      "to-home-r-3": "HOME_R",
      "to-sweep-r-4": "SWEEP_R",
      "to-home-r-5": "HOME_R",
      "to-sweep-r-6": "SWEEP_R",
      "to-home-r-7": "HOME_R",
      "to-sweep-r-8": "SWEEP_R",
      "to-home-r-9": "HOME_R"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-2",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-3",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "TIP 1",
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
        ]
      },
      {
        "id": "w-5",
        "kind": "firstOf",
        "label": "Catch the spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-home-r-1",
        "park": false
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "Our CELL up (1)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 9000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-12",
        "kind": "firstOf",
        "label": "Fire (1)",
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
        "id": "w-13",
        "kind": "firstOf",
        "label": "Did it tip? (1)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-7",
                "kind": "path",
                "lineId": "to-sweep-r-2",
                "park": false
              },
              {
                "id": "w-8",
                "kind": "firstOf",
                "label": "Refill from the line (1)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-9",
                "kind": "path",
                "lineId": "to-home-r-3",
                "park": false
              },
              {
                "id": "w-10",
                "kind": "firstOf",
                "label": "Fire again (1)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "Catch the spill (1)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-19",
        "kind": "firstOf",
        "label": "Our CELL up (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 9000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire (2)",
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
        "id": "w-21",
        "kind": "firstOf",
        "label": "Did it tip? (2)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-15",
                "kind": "path",
                "lineId": "to-sweep-r-4",
                "park": false
              },
              {
                "id": "w-16",
                "kind": "firstOf",
                "label": "Refill from the line (2)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-17",
                "kind": "path",
                "lineId": "to-home-r-5",
                "park": false
              },
              {
                "id": "w-18",
                "kind": "firstOf",
                "label": "Fire again (2)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-22",
        "kind": "firstOf",
        "label": "Catch the spill (2)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-27",
        "kind": "firstOf",
        "label": "Our CELL up (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 9000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-28",
        "kind": "firstOf",
        "label": "Fire (3)",
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
        "id": "w-29",
        "kind": "firstOf",
        "label": "Did it tip? (3)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-23",
                "kind": "path",
                "lineId": "to-sweep-r-6",
                "park": false
              },
              {
                "id": "w-24",
                "kind": "firstOf",
                "label": "Refill from the line (3)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-25",
                "kind": "path",
                "lineId": "to-home-r-7",
                "park": false
              },
              {
                "id": "w-26",
                "kind": "firstOf",
                "label": "Fire again (3)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "Catch the spill (3)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-35",
        "kind": "firstOf",
        "label": "Our CELL up (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 9000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-36",
        "kind": "firstOf",
        "label": "Fire (4)",
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
        "id": "w-37",
        "kind": "firstOf",
        "label": "Did it tip? (4)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-31",
                "kind": "path",
                "lineId": "to-sweep-r-8",
                "park": false
              },
              {
                "id": "w-32",
                "kind": "firstOf",
                "label": "Refill from the line (4)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-33",
                "kind": "path",
                "lineId": "to-home-r-9",
                "park": false
              },
              {
                "id": "w-34",
                "kind": "firstOf",
                "label": "Fire again (4)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-38",
        "kind": "firstOf",
        "label": "Catch the spill (4)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}