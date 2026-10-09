{
  "startPoint": {
    "x": 59,
    "y": 8.06,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-sweep-r-1",
      "color": "#3cc8e4",
      "name": "START to SWEEP_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 20
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-w-s-2",
      "color": "#3cc8e4",
      "name": "SWEEP_R to W_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 22
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-w-n-3",
      "color": "#3cc8e4",
      "name": "W_S to W_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 112
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-sweep-l-4",
      "color": "#3cc8e4",
      "name": "W_N to SWEEP_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 121.5
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-w-n-5",
      "color": "#3cc8e4",
      "name": "SWEEP_L to W_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 112
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-w-s-6",
      "color": "#3cc8e4",
      "name": "W_N to W_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 22
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-sweep-r-7",
      "color": "#3cc8e4",
      "name": "W_S to SWEEP_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 20
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
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
      "lineId": "to-sweep-r-1"
    },
    {
      "kind": "path",
      "lineId": "to-w-s-2"
    },
    {
      "kind": "path",
      "lineId": "to-w-n-3"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-l-4"
    },
    {
      "kind": "path",
      "lineId": "to-w-n-5"
    },
    {
      "kind": "path",
      "lineId": "to-w-s-6"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-r-7"
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
    "exportName": "flow-solo",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll",
        "IntakeOn",
        "StreamOn",
        "CollectSeen",
        "StreamOff"
      ],
      "conditions": [
        "Empty",
        "Tip",
        "IntakeFull"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0,
        "IntakeOn": 0.1,
        "StreamOn": 1.0,
        "CollectSeen": 2.0,
        "StreamOff": 1.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        8.06,
        90
      ],
      "SWEEP_R": [
        50,
        20,
        90
      ],
      "SWEEP_L": [
        50,
        121.5,
        270
      ],
      "W_S": [
        22,
        22,
        90
      ],
      "W_N": [
        30,
        112,
        90
      ]
    },
    "pathEnds": {
      "to-sweep-r-1": "SWEEP_R",
      "to-w-s-2": "W_S",
      "to-w-n-3": "W_N",
      "to-sweep-l-4": "SWEEP_L",
      "to-w-n-5": "W_N",
      "to-w-s-6": "W_S",
      "to-sweep-r-7": "SWEEP_R"
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
        "label": "Fire the preloads (TIP 1)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-sweep-r-1",
        "park": false
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "TIP 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 4000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-5",
        "kind": "firstOf",
        "label": "TIP 1's spill lands",
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
        "id": "a-6",
        "kind": "action",
        "name": "IntakeOn"
      },
      {
        "id": "a-7",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-10",
        "kind": "firstOf",
        "label": "Sweep TIP 1's spill, streaming at the left CELL (TIP 2), pass 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": [
              {
                "id": "w-9",
                "kind": "firstOf",
                "label": "Sweep TIP 1's spill, streaming at the left CELL (TIP 2), pass 2",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 3000,
                    "cards": [
                      {
                        "id": "w-8",
                        "kind": "firstOf",
                        "label": "Sweep TIP 1's spill, streaming at the left CELL (TIP 2), pass 3",
                        "rows": [
                          {
                            "when": [
                              "Tip"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 3000,
                            "cards": []
                          }
                        ],
                        "alongside": "CollectSeen"
                      }
                    ]
                  }
                ],
                "alongside": "CollectSeen"
              }
            ]
          }
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "a-11",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-w-s-2",
        "park": false
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-w-n-3",
        "park": false
      },
      {
        "id": "p-14",
        "kind": "path",
        "lineId": "to-sweep-l-4",
        "park": false
      },
      {
        "id": "w-15",
        "kind": "firstOf",
        "label": "TIP 2's spill lands",
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
        "id": "a-16",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-19",
        "kind": "firstOf",
        "label": "Sweep TIP 2's spill, streaming at the right CELL (TIP 3), pass 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": [
              {
                "id": "w-18",
                "kind": "firstOf",
                "label": "Sweep TIP 2's spill, streaming at the right CELL (TIP 3), pass 2",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 3000,
                    "cards": [
                      {
                        "id": "w-17",
                        "kind": "firstOf",
                        "label": "Sweep TIP 2's spill, streaming at the right CELL (TIP 3), pass 3",
                        "rows": [
                          {
                            "when": [
                              "Tip"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 3000,
                            "cards": []
                          }
                        ],
                        "alongside": "CollectSeen"
                      }
                    ]
                  }
                ],
                "alongside": "CollectSeen"
              }
            ]
          }
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "a-20",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-21",
        "kind": "path",
        "lineId": "to-w-n-5",
        "park": false
      },
      {
        "id": "p-22",
        "kind": "path",
        "lineId": "to-w-s-6",
        "park": false
      },
      {
        "id": "p-23",
        "kind": "path",
        "lineId": "to-sweep-r-7",
        "park": false
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "TIP 3's spill lands",
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
        "id": "a-25",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-28",
        "kind": "firstOf",
        "label": "Sweep TIP 3's spill, streaming at the left CELL (TIP 4), pass 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": [
              {
                "id": "w-27",
                "kind": "firstOf",
                "label": "Sweep TIP 3's spill, streaming at the left CELL (TIP 4), pass 2",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 3000,
                    "cards": [
                      {
                        "id": "w-26",
                        "kind": "firstOf",
                        "label": "Sweep TIP 3's spill, streaming at the left CELL (TIP 4), pass 3",
                        "rows": [
                          {
                            "when": [
                              "Tip"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 3000,
                            "cards": []
                          }
                        ],
                        "alongside": "CollectSeen"
                      }
                    ]
                  }
                ],
                "alongside": "CollectSeen"
              }
            ]
          }
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "a-29",
        "kind": "action",
        "name": "StreamOff"
      }
    ]
  },
  "version": "1.5.0"
}