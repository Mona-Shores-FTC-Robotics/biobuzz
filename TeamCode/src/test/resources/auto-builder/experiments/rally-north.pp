{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-flower-n-1",
      "color": "#3cc8e4",
      "name": "START to FLOWER_N",
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
      "id": "to-home-n-2",
      "color": "#3cc8e4",
      "name": "FLOWER_N to HOME_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
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
      "id": "to-sweep-n-3",
      "color": "#3cc8e4",
      "name": "HOME_N to SWEEP_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-home-n-4",
      "color": "#3cc8e4",
      "name": "SWEEP_N to HOME_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-sweep-n-5",
      "color": "#3cc8e4",
      "name": "HOME_N to SWEEP_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-home-n-6",
      "color": "#3cc8e4",
      "name": "SWEEP_N to HOME_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-sweep-n-7",
      "color": "#3cc8e4",
      "name": "HOME_N to SWEEP_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-home-n-8",
      "color": "#3cc8e4",
      "name": "SWEEP_N to HOME_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-sweep-n-9",
      "color": "#3cc8e4",
      "name": "HOME_N to SWEEP_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-home-n-10",
      "color": "#3cc8e4",
      "name": "SWEEP_N to HOME_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
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
      "lineId": "to-flower-n-1"
    },
    {
      "kind": "path",
      "lineId": "to-home-n-2"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-n-3"
    },
    {
      "kind": "path",
      "lineId": "to-home-n-4"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-n-5"
    },
    {
      "kind": "path",
      "lineId": "to-home-n-6"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-n-7"
    },
    {
      "kind": "path",
      "lineId": "to-home-n-8"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-n-9"
    },
    {
      "kind": "path",
      "lineId": "to-home-n-10"
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
    "exportName": "rally-north",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll"
      ],
      "conditions": [
        "LeftCellUp",
        "Empty",
        "IntakeFull",
        "RightCellUp"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        132.25,
        270
      ],
      "FLOWER_N": [
        47.4,
        127.8,
        90
      ],
      "HOME_N": [
        59,
        131.75,
        270
      ],
      "SWEEP_N": [
        45,
        131.75,
        270
      ]
    },
    "pathEnds": {
      "to-flower-n-1": "FLOWER_N",
      "to-home-n-2": "HOME_N",
      "to-sweep-n-3": "SWEEP_N",
      "to-home-n-4": "HOME_N",
      "to-sweep-n-5": "SWEEP_N",
      "to-home-n-6": "HOME_N",
      "to-sweep-n-7": "SWEEP_N",
      "to-home-n-8": "HOME_N",
      "to-sweep-n-9": "SWEEP_N",
      "to-home-n-10": "HOME_N"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "w-2",
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
            "afterMs": 7500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "Fire the preloads",
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
        "id": "p-4",
        "kind": "path",
        "lineId": "to-flower-n-1",
        "park": false
      },
      {
        "id": "w-5",
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
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-home-n-2",
        "park": false
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Fire (TIP 2)",
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
      },
      {
        "id": "w-8",
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
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-13",
        "kind": "firstOf",
        "label": "Our CELL up (1)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-14",
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
        "id": "w-15",
        "kind": "firstOf",
        "label": "Did it tip? (1)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-9",
                "kind": "path",
                "lineId": "to-sweep-n-3",
                "park": false
              },
              {
                "id": "w-10",
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
                "id": "p-11",
                "kind": "path",
                "lineId": "to-home-n-4",
                "park": false
              },
              {
                "id": "w-12",
                "kind": "firstOf",
                "label": "Fire again (1)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-16",
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
        "id": "w-21",
        "kind": "firstOf",
        "label": "Our CELL up (2)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-22",
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
        "id": "w-23",
        "kind": "firstOf",
        "label": "Did it tip? (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-17",
                "kind": "path",
                "lineId": "to-sweep-n-5",
                "park": false
              },
              {
                "id": "w-18",
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
                "id": "p-19",
                "kind": "path",
                "lineId": "to-home-n-6",
                "park": false
              },
              {
                "id": "w-20",
                "kind": "firstOf",
                "label": "Fire again (2)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-24",
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
        "id": "w-29",
        "kind": "firstOf",
        "label": "Our CELL up (3)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-30",
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
        "id": "w-31",
        "kind": "firstOf",
        "label": "Did it tip? (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-25",
                "kind": "path",
                "lineId": "to-sweep-n-7",
                "park": false
              },
              {
                "id": "w-26",
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
                "id": "p-27",
                "kind": "path",
                "lineId": "to-home-n-8",
                "park": false
              },
              {
                "id": "w-28",
                "kind": "firstOf",
                "label": "Fire again (3)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-32",
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
        "id": "w-37",
        "kind": "firstOf",
        "label": "Our CELL up (4)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-38",
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
        "id": "w-39",
        "kind": "firstOf",
        "label": "Did it tip? (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [],
            "label": "Yes: stay and catch"
          },
          {
            "afterMs": 900,
            "cards": [
              {
                "id": "p-33",
                "kind": "path",
                "lineId": "to-sweep-n-9",
                "park": false
              },
              {
                "id": "w-34",
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
                "id": "p-35",
                "kind": "path",
                "lineId": "to-home-n-10",
                "park": false
              },
              {
                "id": "w-36",
                "kind": "firstOf",
                "label": "Fire again (4)",
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
            "label": "No: refill from the line"
          }
        ]
      },
      {
        "id": "w-40",
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